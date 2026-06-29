package com.swmanager.system.license.sync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * License4J Derby DB 읽기 (P3).
 *
 * 라이브 DB는 License4J Manager가 단일 점유 잠금 → 절대 직결하지 않고,
 * 폴더를 사본으로 복사(= 그 달의 Derby 백업본)한 뒤 read-only 부팅하여 SELECT 한다.
 *
 * 컬럼 매핑은 본 Reader가 관여하지 않는다(매핑 독립). {@code SELECT l.*, p."productIDString"}
 * 로 raw row(컬럼명→값 Map)를 반환하고, 코드→라벨/63컬럼 매핑은 LicenseFieldMapper(P5)가 담당.
 * APP.PRODUCTS."licenseKeyPair"(개인키)는 LICENSES.* 에 포함되지 않으므로 자연히 미조회(NFR-2).
 *
 * P0 게이트: derby 10.14.2.0 ↔ License4J Derby 10.10 readOnly 부팅 호환 검증 완료(2026-06-29).
 */
@Slf4j
@Component
public class DerbyLicenseReader {

    /** 라이선스 서버 license4jdb 폴더 UNC 경로 */
    @Value("${license4j.sync.derby-unc:}")
    private String derbyUnc;

    /** 월별 Derby 백업본 보관 루트 */
    @Value("${license4j.sync.backup-dir:}")
    private String backupDir;

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    static {
        // 임베디드 드라이버 등록
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Derby EmbeddedDriver 미존재 — pom 의존성 확인", e);
        }
    }

    /** 복사+읽기 결과 */
    public record ReadResult(List<Map<String, Object>> rows, Path backupPath) {}

    /**
     * 라이선스 서버 Derby 폴더를 백업 경로로 복사하고, 사본을 read-only로 읽어 raw row 목록을 반환.
     * @param stampSeed 백업본 폴더명 타임스탬프(스케줄/수동 트리거 시각). 스크립트 결정성 위해 외부 주입.
     */
    public ReadResult copyAndRead(LocalDateTime stampSeed) throws IOException, SQLException {
        if (derbyUnc == null || derbyUnc.isBlank())
            throw new IllegalStateException("license4j.sync.derby-unc 미설정");
        if (backupDir == null || backupDir.isBlank())
            throw new IllegalStateException("license4j.sync.backup-dir 미설정");

        Path src = Path.of(derbyUnc);
        if (!Files.isDirectory(src))
            throw new IOException("Derby 소스 폴더 접근 불가(권한/네트워크 확인): " + maskPath(derbyUnc));

        Path backup = Path.of(backupDir, "license4jdb_" + stampSeed.format(STAMP));
        copyTree(src, backup);
        // 사본의 stale 잠금 제거(원본 무관) — read-only 부팅 방해 방지
        Files.deleteIfExists(backup.resolve("db.lck"));

        List<Map<String, Object>> rows = readReadOnly(backup);
        log.info("Derby 읽기 완료 — {}건, 백업본={}", rows.size(), backup.getFileName());
        return new ReadResult(rows, backup);
    }

    /** 사본을 read-only 부팅하여 APP.LICENSES ⨝ PRODUCTS 전 컬럼(+productIDString) 조회 */
    private List<Map<String, Object>> readReadOnly(Path derbyDir) throws SQLException {
        String dbPath = derbyDir.toAbsolutePath().toString().replace('\\', '/');
        String url = "jdbc:derby:" + dbPath + ";readOnly=true";
        // Product ID(대장)는 productName(+productVersion) 형식 — productIDString 아님(P4 실증/실연동 검증).
        String sql = "SELECT l.*, p.\"productName\", p.\"productVersion\" "
                   + "FROM APP.LICENSES l JOIN APP.PRODUCTS p ON l.\"productID\" = p.\"productID\"";
        List<Map<String, Object>> rows = new ArrayList<>();
        try {
            try (Connection c = DriverManager.getConnection(url);
                 Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                ResultSetMetaData md = rs.getMetaData();
                int n = md.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>(n * 2);
                    for (int i = 1; i <= n; i++) {
                        row.put(md.getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
            }
        } finally {
            shutdownDerby(dbPath);
        }
        return rows;
    }

    /** 임베디드 Derby 인스턴스 종료(파일 잠금 해제 — 다음 실행/사본 정리 가능하게). */
    private void shutdownDerby(String dbPath) {
        try {
            DriverManager.getConnection("jdbc:derby:" + dbPath + ";shutdown=true");
        } catch (SQLException e) {
            // 정상 종료는 SQLState 08006/XJ015 예외로 통지됨 — 무시
            if (!"08006".equals(e.getSQLState()) && !"XJ015".equals(e.getSQLState()))
                log.warn("Derby 종료 경고: {}", e.getSQLState());
        }
    }

    /** 재귀 폴더 복사 (NIO). OS 레벨 SMB 세션으로 UNC 접근(운영 호스트가 .188 접근 가능 전제). */
    private void copyTree(Path src, Path dest) throws IOException {
        Files.createDirectories(dest);
        Files.walkFileTree(src, new SimpleFileVisitor<>() {
            @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes a) throws IOException {
                Files.createDirectories(dest.resolve(src.relativize(dir).toString()));
                return FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes a) throws IOException {
                Files.copy(file, dest.resolve(src.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /** 경로 로그 마스킹(NFR-2) — 호스트/계정 노출 최소화 */
    private String maskPath(String p) {
        if (p == null) return "";
        return p.replaceFirst("^(\\\\\\\\[^\\\\]+).*", "$1\\\\***");
    }
}
