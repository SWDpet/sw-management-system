package com.swmanager.system.geonuris.service;

import com.swmanager.system.geonuris.domain.GeonurisLicense;
import com.swmanager.system.geonuris.repository.GeonurisLicenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeonurisLicenseService {

    private final GeonurisLicenseRepository repository;

    @Value("${geonuris.license.jar.path:}")
    private String jarPathConfig;

    private volatile Path cachedJarPath = null;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ── 메타 ────────────────────────────────────────────────────

    public record LicenseMeta(int targetNum, String fileName, String isitValue) {}

    public static LicenseMeta getMeta(String licenseType) {
        return switch (licenseType) {
            case "GSS30"      -> new LicenseMeta(1, "GSS.lic",             "GSS 3.0");
            case "GSS35"      -> new LicenseMeta(2, "GSS.lic",             "GSS 3.5");
            case "DESKTOP"    -> new LicenseMeta(3, "GeoNURISDesktop.lic", "Desktop");
            case "SETL_AGENT" -> new LicenseMeta(4, "SETLAgent.lic",       "C1004");
            case "SETL_PROXY" -> new LicenseMeta(5, "SETLProxy.lic",       "C1005");
            default -> throw new IllegalArgumentException("알 수 없는 라이선스 타입: " + licenseType);
        };
    }

    // ── 발급 ────────────────────────────────────────────────────

    @Transactional
    public GeonurisLicense issue(GeonurisLicense lic) throws Exception {
        LicenseMeta meta = getMeta(lic.getLicenseType());
        lic.setFileName(meta.fileName());

        byte[] licBytes = generateLicFile(lic, meta);
        lic.setLicenseData(Base64.getEncoder().encodeToString(licBytes));
        lic.setCreatedAt(LocalDateTime.now());

        GeonurisLicense saved = repository.save(lic);
        log.info("GeoNURIS 라이선스 발급 완료 - ID:{} 타입:{} 사용자:{}",
                saved.getId(), saved.getLicenseType(), saved.getUserName());
        return saved;
    }

    // ── JAR 경로 ─────────────────────────────────────────────────

    private Path resolveJarPath() throws IOException {
        // 1) application.properties에 명시된 외부 경로가 있고, 실제 파일이 존재하면 사용
        if (jarPathConfig != null && !jarPathConfig.isBlank()) {
            Path p = Path.of(jarPathConfig);
            if (Files.exists(p)) {
                log.info("외부 JAR 경로 사용: {}", p);
                return p;
            }
            // 외부 경로에 파일이 없으면 classpath fallback으로 진행
            log.warn("설정된 JAR 경로에 파일 없음({}), classpath에서 추출 시도", jarPathConfig);
        }

        // 2) 캐시된 임시 파일이 있으면 재사용
        if (cachedJarPath != null && Files.exists(cachedJarPath))
            return cachedJarPath;

        // 3) classpath(resources)에서 JAR 추출 → WAR/JAR 배포 모두 지원
        String res = "/geonuris/GeoNURIS_License.jar";
        try (InputStream is = getClass().getResourceAsStream(res)) {
            if (is == null)
                throw new FileNotFoundException(
                    "GeoNURIS_License.jar 없음.\n" +
                    "src/main/resources/geonuris/ 에 파일을 배치하거나,\n" +
                    "geonuris.license.jar.path 설정에 절대경로를 지정하세요.");
            Path tmp = Files.createTempFile("GeoNURIS_License_", ".jar");
            tmp.toFile().deleteOnExit();
            Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
            log.info("GeoNURIS_License.jar classpath에서 추출 완료: {}", tmp);
            cachedJarPath = tmp;
            return tmp;
        }
    }

    // ── URLClassLoader + 리플렉션 ─────────────────────────────────

    /**
     * LicenseVo 필드 타입 (역분석 결과)
     *
     * String 필드: fileName, ISIT, ISDT, EXDT, MACA, PERM, SETL, DBMS
     * int 필드:    D_TMBD(1/2), D_SETL(1/2), D_EDIT(1/2), D_GDM(1/2)
     *
     * ★ 핵심: SETL 필드는 모든 타입에서 반드시 세팅해야 함
     *   (LicenseGenerator.getLicenseData() 에서 getSETL().trim() 호출)
     *   Desktop/GSS 등 SETL 미사용 타입은 "0" 으로 기본값 세팅
     */
    private byte[] generateLicFile(GeonurisLicense lic, LicenseMeta meta) throws Exception {
        Path jarFile = resolveJarPath();
        log.info("URLClassLoader JAR: {}", jarFile);

        try (URLClassLoader cl = new URLClassLoader(
                new URL[]{ jarFile.toUri().toURL() },
                ClassLoader.getPlatformClassLoader())) {

            Class<?> voClass = cl.loadClass("LicenseVo");
            Object vo = voClass.getDeclaredConstructor().newInstance();

            // ── 공통 필드 (모든 타입) ──────────────────────────────
            setStr(voClass, vo, "ISIT",     meta.isitValue());
            setStr(voClass, vo, "MACA",     lic.getMacAddress());
            setStr(voClass, vo, "ISDT",     lic.getStartDate().format(DATE_FMT));
            setStr(voClass, vo, "EXDT",     lic.getExpiryDate().format(DATE_FMT));
            setStr(voClass, vo, "fileName", meta.fileName());

            // ★ SETL 은 모든 타입에서 필수 세팅 (null이면 getLicenseData에서 NPE)
            String setlVal = (lic.getSetlCount() != null) ? String.valueOf(lic.getSetlCount()) : "0";
            setStr(voClass, vo, "SETL", setlVal);

            // ── 타입별 추가 필드 ───────────────────────────────────
            switch (lic.getLicenseType()) {
                case "GSS30", "GSS35" -> {
                    setStr(voClass, vo, "DBMS", dbmsLabel(lic.getDbmsType()));
                    // Desktop 플러그인 int 필드도 기본값 1로 세팅
                    setInt(voClass, vo, "D_EDIT",  1);
                    setInt(voClass, vo, "D_GDM",   1);
                    setInt(voClass, vo, "D_TMBD",  1);
                    setInt(voClass, vo, "D_SETL",  1);
                }
                case "DESKTOP" -> {
                    setStr(voClass, vo, "PERM", safe(lic.getPermission(), "A"));
                    // ★ int 필드: 1=미허용, 2=허용
                    setInt(voClass, vo, "D_EDIT",  bool2int(lic.getPluginEdit()));
                    setInt(voClass, vo, "D_GDM",   bool2int(lic.getPluginGdm()));
                    setInt(voClass, vo, "D_TMBD",  bool2int(lic.getPluginTmbuilder()));
                    setInt(voClass, vo, "D_SETL",  bool2int(lic.getPluginSetl()));
                }
                case "SETL_AGENT", "SETL_PROXY" -> {
                    setStr(voClass, vo, "DBMS", dbmsLabel(lic.getDbmsType()));
                    setInt(voClass, vo, "D_EDIT",  1);
                    setInt(voClass, vo, "D_GDM",   1);
                    setInt(voClass, vo, "D_TMBD",  1);
                    setInt(voClass, vo, "D_SETL",  1);
                }
            }

            log.debug("LicenseVo 세팅 완료");

            // ── createLicense(LicenseVo) 호출 ─────────────────────
            // 시그니처: public static byte[] LicenseGenerator.createLicense(LicenseVo)
            Class<?> genClass = cl.loadClass("LicenseGenerator");
            Method createMethod = genClass.getDeclaredMethod("createLicense", voClass);
            createMethod.setAccessible(true);

            log.info("createLicense 호출: {}", createMethod);
            byte[] result = (byte[]) createMethod.invoke(null, vo);

            if (result == null || result.length == 0)
                throw new RuntimeException("createLicense 반환값이 비어있습니다.");

            log.info("라이선스 생성 성공: {} bytes", result.length);
            return result;
        }
    }

    // ── 헬퍼 ────────────────────────────────────────────────────

    /**
     * String 타입 필드 직접 세팅 (필드명으로 접근, setter 불필요)
     * LicenseVo 는 모든 String 필드가 public이므로 필드 직접 접근
     */
    private void setStr(Class<?> cls, Object obj, String fieldName, String value) {
        try {
            Field f = cls.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(obj, value != null ? value : "");
            log.debug("LicenseVo.{} = \"{}\"", fieldName, value);
        } catch (NoSuchFieldException e) {
            // setter 방식 fallback
            try {
                Method m = cls.getDeclaredMethod("set" + fieldName, String.class);
                m.setAccessible(true);
                m.invoke(obj, value != null ? value : "");
                log.debug("LicenseVo.set{}(\"{}\") [setter]", fieldName, value);
            } catch (Exception ex) {
                log.warn("LicenseVo.{} 세팅 실패: {}", fieldName, ex.getMessage());
            }
        } catch (Exception e) {
            log.warn("LicenseVo.{} 세팅 실패: {}", fieldName, e.getMessage());
        }
    }

    /**
     * int 타입 필드 직접 세팅
     * D_EDIT, D_GDM, D_TMBD, D_SETL 은 모두 int
     */
    private void setInt(Class<?> cls, Object obj, String fieldName, int value) {
        try {
            Field f = cls.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.setInt(obj, value);
            log.debug("LicenseVo.{} = {}", fieldName, value);
        } catch (NoSuchFieldException e) {
            log.warn("LicenseVo.{} int 필드 없음", fieldName);
        } catch (Exception e) {
            log.warn("LicenseVo.{} int 세팅 실패: {}", fieldName, e.getMessage());
        }
    }

    private String dbmsLabel(String type) {
        if (type == null) return "ORACLE";
        return switch (type.toLowerCase()) {
            case "oracle"   -> "ORACLE";
            case "tibero"   -> "TIBERO";
            case "mssql"    -> "MS-SQL";
            case "altibase" -> "ALTIBASE";
            default         -> "ORACLE";
        };
    }

    /** boolean → int (1=미허용, 2=허용) */
    private int bool2int(Boolean b) { return (b != null && b) ? 2 : 1; }
    private String safe(String v, String def) { return (v != null && !v.isBlank()) ? v.trim() : def; }

    // ── 수정 (라이선스 재생성) ────────────────────────────────────

    @Transactional
    public GeonurisLicense update(GeonurisLicense lic) throws Exception {
        LicenseMeta meta = getMeta(lic.getLicenseType());
        lic.setFileName(meta.fileName());

        // 라이선스 파일 재생성
        byte[] licBytes = generateLicFile(lic, meta);
        lic.setLicenseData(Base64.getEncoder().encodeToString(licBytes));

        GeonurisLicense saved = repository.save(lic);
        log.info("GeoNURIS 라이선스 수정 완료 - ID:{} 타입:{} 사용자:{}",
                saved.getId(), saved.getLicenseType(), saved.getUserName());
        return saved;
    }

    // ── 삭제 ────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        GeonurisLicense lic = getById(id);
        repository.delete(lic);
        log.info("GeoNURIS 라이선스 삭제 완료 - ID:{} 타입:{} 사용자:{}",
                lic.getId(), lic.getLicenseType(), lic.getUserName());
    }

    // ── 조회 ────────────────────────────────────────────────────

    public List<GeonurisLicense> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public List<GeonurisLicense> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return findAll();
        return repository.searchByKeyword(keyword.trim());
    }

    public GeonurisLicense getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("라이선스 없음: id=" + id));
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total",      repository.count());
        stats.put("gss30",      repository.countByLicenseType("GSS30"));
        stats.put("gss35",      repository.countByLicenseType("GSS35"));
        stats.put("desktop",    repository.countByLicenseType("DESKTOP"));
        stats.put("setlAgent",  repository.countByLicenseType("SETL_AGENT"));
        stats.put("setlProxy",  repository.countByLicenseType("SETL_PROXY"));
        return stats;
    }

    public byte[] getLicenseBytes(GeonurisLicense lic) {
        if (lic.getLicenseData() == null)
            throw new RuntimeException("라이선스 파일 데이터 없음");
        return Base64.getDecoder().decode(lic.getLicenseData());
    }
}
