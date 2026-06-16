package com.swmanager.system.service.ops;

import com.swmanager.system.constant.enums.OpsDocType;
import com.swmanager.system.domain.SigunguCode;
import com.swmanager.system.domain.SysMst;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.ops.OpsDocument;
import com.swmanager.system.domain.ops.OpsDocumentDetail;
import com.swmanager.system.repository.SigunguCodeRepository;
import com.swmanager.system.repository.SysMstRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.ops.OpsDocumentDetailRepository;
import com.swmanager.system.repository.ops.OpsDocumentRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 업무지원(SUPPORT) 지원문서 단일파일 저장 서비스.
 * 기획서: docs/product-specs/ops-support-doc-upload.md (v0.3)
 * 개발계획: docs/exec-plans/ops-support-doc-upload.md (v0.2)
 *
 * 착수계 날인본(DocumentSignedScanService) 패턴 이식 — 1:1 교체, 경로안전(FR-9),
 * 다형식(한글/엑셀/워드/PDF) 매직바이트 검증(FR-5), 원자적 교체+롤백(FR-10), SUPPORT 타입가드(FR-8).
 * 경로: {base}/업무지원/{년도}/{시스템}/{시도}/{시군구}/{지원대상}_{docNo}.{ext}
 */
@Slf4j
@Service
@Transactional
public class OpsSupportFileService {

    /** 허용 확장자(소문자). */
    private static final Set<String> ALLOWED_EXT =
            Set.of("hwp", "hwpx", "xls", "xlsx", "doc", "docx", "pdf");
    /** ZIP 컨테이너 계열(엔트리 보유 로컬헤더만 허용) — hwpx/xlsx/docx. */
    private static final Set<String> ZIP_EXT = Set.of("hwpx", "xlsx", "docx");
    /** OLE2 복합문서 계열 — hwp/xls/doc. */
    private static final Set<String> OLE2_EXT = Set.of("hwp", "xls", "doc");

    private static final byte[] SIG_PDF  = { 0x25, 0x50, 0x44, 0x46, 0x2D };                                  // %PDF-
    private static final byte[] SIG_ZIP  = { 0x50, 0x4B, 0x03, 0x04 };                                        // PK\3\4 (엔트리 보유)
    private static final byte[] SIG_OLE2 = { (byte)0xD0, (byte)0xCF, 0x11, (byte)0xE0,
                                             (byte)0xA1, (byte)0xB1, 0x1A, (byte)0xE1 };                       // OLE2

    private static final int MAX_NAME_LEN = 80;
    private static final long MAX_BYTES = 30L * 1024 * 1024; // 30MB

    private final OpsDocumentRepository opsDocumentRepository;
    private final OpsDocumentDetailRepository opsDocumentDetailRepository;
    private final SigunguCodeRepository sigunguCodeRepository;
    private final SysMstRepository sysMstRepository;
    private final UserRepository userRepository;

    @Value("${file.scan-dir:./uploads/scan}")
    private String scanDir;

    public OpsSupportFileService(OpsDocumentRepository opsDocumentRepository,
                                 OpsDocumentDetailRepository opsDocumentDetailRepository,
                                 SigunguCodeRepository sigunguCodeRepository,
                                 SysMstRepository sysMstRepository,
                                 UserRepository userRepository) {
        this.opsDocumentRepository = opsDocumentRepository;
        this.opsDocumentDetailRepository = opsDocumentDetailRepository;
        this.sigunguCodeRepository = sigunguCodeRepository;
        this.sysMstRepository = sysMstRepository;
        this.userRepository = userRepository;
    }

    private Path baseDir() {
        return Paths.get(scanDir).toAbsolutePath().normalize();
    }

    /** SUPPORT 문서만 지원파일을 다룬다(FR-8 타입가드). */
    private void requireSupport(OpsDocument doc) {
        if (doc.getDocType() != OpsDocType.SUPPORT)
            throw new IllegalStateException("업무지원(SUPPORT) 문서만 지원문서를 첨부할 수 있습니다.");
    }

    /**
     * 업로드(또는 교체). 타입가드(FR-8)·형식검증(FR-5)·경로안전(FR-9)·원자적 교체+롤백(FR-10).
     */
    @Transactional(rollbackFor = Exception.class)
    public OpsDocument uploadOrReplace(Long docId, MultipartFile file, Long uploaderSeq) throws IOException {
        OpsDocument doc = opsDocumentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + docId));
        requireSupport(doc);

        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        if (file.getSize() > MAX_BYTES)
            throw new IllegalArgumentException("파일이 너무 큽니다(최대 30MB).");

        String ext = extOf(file.getOriginalFilename());
        if (!ALLOWED_EXT.contains(ext))
            throw new IllegalArgumentException("허용되지 않는 형식입니다(한글/엑셀/워드/PDF만 가능): " + ext);

        Path base = baseDir();
        Path dir = base.resolve("업무지원")
                .resolve(yearFolder(doc))
                .resolve(seg(systemName(doc), "시스템미상"))
                .resolve(seg(sidoName(doc), "시도미상"))
                .resolve(seg(sggName(doc), "시군구미상"))
                .normalize();
        if (!dir.startsWith(base)) throw new SecurityException("잘못된 저장 경로입니다.");
        Files.createDirectories(dir);
        // FR-9 symlink/실경로 우회 차단
        if (!dir.toRealPath().startsWith(base.toRealPath()))
            throw new SecurityException("잘못된 저장 경로입니다.");

        String fileName = seg(supportTarget(doc), "지원대상미상") + "_" + seg(doc.getDocNo(), "docno") + "." + ext;
        Path target = dir.resolve(fileName).normalize();
        if (!target.startsWith(base)) throw new SecurityException("잘못된 파일 경로입니다.");

        // FR-5 스트리밍 temp 작성 + 선두 매직바이트 검증
        Path temp = Files.createTempFile(dir, ".upload-", ".part");
        try {
            try (InputStream in = new BufferedInputStream(file.getInputStream())) {
                in.mark(16);
                byte[] head = in.readNBytes(8);
                if (!magicOk(ext, head))
                    throw new IllegalArgumentException("파일 형식이 확장자와 일치하지 않습니다(위조 의심).");
                in.reset();
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            }

            // FR-10 ① DB 먼저 flush
            String prev = doc.getSupportFilePath();
            doc.setSupportFilePath(target.toString());
            doc.setSupportFileOrigName(file.getOriginalFilename());
            doc.setSupportFileExt(ext);
            doc.setSupportFileSize(file.getSize());
            doc.setSupportFileUploadedAt(LocalDateTime.now());
            if (uploaderSeq != null) {
                User u = userRepository.findById(uploaderSeq).orElse(null);
                doc.setSupportFileUploadedBy(u);
            }
            opsDocumentRepository.saveAndFlush(doc);

            // FR-10 ② 기존 target 백업 후 원자 이동
            Path backup = null;
            if (Files.exists(target)) {
                backup = target.resolveSibling(target.getFileName().toString() + ".bak-" + System.nanoTime());
                Files.move(target, backup, StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                try {
                    Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
                }
                if (backup != null) Files.deleteIfExists(backup);
            } catch (IOException moveErr) {
                if (backup != null) {
                    try { Files.move(backup, target, StandardCopyOption.REPLACE_EXISTING); } catch (IOException ignored) {}
                }
                log.warn("[ops-support-file] move 실패 docId={} target={}", docId, target, moveErr);
                throw moveErr; // rollbackFor=Exception → DB 롤백
            }

            // 이전(다른 경로) 파일 정리 — 1:1 고아 방지
            if (prev != null) {
                Path prevPath = Paths.get(prev).normalize();
                if (prevPath.startsWith(base) && !prevPath.equals(target)) {
                    try { Files.deleteIfExists(prevPath); } catch (IOException ignored) {}
                }
            }
            return doc;
        } finally {
            try { Files.deleteIfExists(temp); } catch (IOException ignored) {}
        }
    }

    /** 지원문서 삭제 (파일 + 메타). 타입가드 동일. */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long docId) {
        OpsDocument doc = opsDocumentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + docId));
        requireSupport(doc);
        String prev = doc.getSupportFilePath();
        if (prev != null) {
            Path base = baseDir();
            Path p = Paths.get(prev).normalize();
            if (p.startsWith(base)) {
                try {
                    if (!Files.exists(p) || p.toRealPath().startsWith(base.toRealPath()))
                        Files.deleteIfExists(p);
                } catch (IOException ignored) {}
            }
        }
        doc.setSupportFilePath(null);
        doc.setSupportFileOrigName(null);
        doc.setSupportFileExt(null);
        doc.setSupportFileSize(null);
        doc.setSupportFileUploadedAt(null);
        doc.setSupportFileUploadedBy(null);
        opsDocumentRepository.save(doc);
    }

    /** 다운로드 리소스 (경로 안전 + symlink 실경로 재검증 FR-9). */
    @Transactional(readOnly = true)
    public Resource loadForDownload(Long docId) {
        OpsDocument doc = opsDocumentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + docId));
        requireSupport(doc);
        String path = doc.getSupportFilePath();
        if (path == null) throw new IllegalArgumentException("지원문서가 없습니다.");
        Path base = baseDir();
        Path p = Paths.get(path).normalize();
        if (!p.startsWith(base)) throw new SecurityException("잘못된 파일 경로입니다.");
        if (!Files.exists(p)) throw new IllegalArgumentException("파일이 존재하지 않습니다.");
        try {
            if (!p.toRealPath().startsWith(base.toRealPath())) throw new SecurityException("잘못된 파일 경로입니다.");
        } catch (IOException e) {
            throw new IllegalArgumentException("파일 접근 오류");
        }
        return new FileSystemResource(p);
    }

    @Transactional(readOnly = true)
    public String originalName(Long docId) {
        return opsDocumentRepository.findById(docId)
                .map(OpsDocument::getSupportFileOrigName)
                .orElse("support-file");
    }

    // === helpers ===

    /** 확장자 추출 + 소문자 normalize (FR-3 사소). */
    static String extOf(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT).trim();
    }

    /** 확장자 계열별 선두 매직바이트 검증. head 가 시그니처보다 짧으면 거부. */
    static boolean magicOk(String ext, byte[] head) {
        if (head == null) return false;
        if ("pdf".equals(ext))       return startsWith(head, SIG_PDF);
        if (ZIP_EXT.contains(ext))   return startsWith(head, SIG_ZIP);   // 빈 ZIP(PK\5\6) 등은 불허
        if (OLE2_EXT.contains(ext))  return startsWith(head, SIG_OLE2);
        return false;
    }

    private static boolean startsWith(byte[] data, byte[] sig) {
        if (data.length < sig.length) return false;
        for (int i = 0; i < sig.length; i++) if (data[i] != sig[i]) return false;
        return true;
    }

    /** main section_data 의 값 조회. */
    private String sectionValue(OpsDocument doc, String key) {
        List<OpsDocumentDetail> details = opsDocumentDetailRepository.findByDocument_DocIdOrderBySortOrderAsc(doc.getDocId());
        for (OpsDocumentDetail d : details) {
            if ("main".equals(d.getSectionKey()) && d.getSectionData() != null) {
                Object v = d.getSectionData().get(key);
                return (v != null) ? v.toString() : null;
            }
        }
        return null;
    }

    private String supportTarget(OpsDocument doc) {
        return sectionValue(doc, "support_target");
    }

    /** 요청일(yyyy-MM-dd) 연도 → 생성일 연도 → '연도미상'. */
    private String yearFolder(OpsDocument doc) {
        String rd = sectionValue(doc, "request_date");
        if (rd != null && rd.length() >= 4) {
            String y = rd.substring(0, 4);
            if (y.chars().allMatch(Character::isDigit)) return y;
        }
        LocalDateTime created = doc.getCreatedAt();
        if (created != null) return String.valueOf(created.getYear());
        return "연도미상";
    }

    private String systemName(OpsDocument doc) {
        String code = doc.getSysType();
        if (code == null || code.isBlank()) return null;
        SysMst s = sysMstRepository.findById(code).orElse(null);
        return (s != null && s.getNm() != null && !s.getNm().isBlank()) ? s.getNm() : code;
    }

    private SigunguCode region(OpsDocument doc) {
        String code = doc.getRegionCode();
        if (code == null || code.isBlank()) return null;
        return sigunguCodeRepository.findById(code).orElse(null);
    }

    private String sidoName(OpsDocument doc) {
        SigunguCode s = region(doc);
        return (s != null) ? s.getSidoNm() : null;
    }

    private String sggName(OpsDocument doc) {
        SigunguCode s = region(doc);
        return (s != null) ? s.getSggNm() : null;
    }

    /** 경로 세그먼트 정제: 금지문자·제어문자 제거 + 공백압축 + 길이컷(80) + 빈값 fallback (착수계 동일). */
    static String seg(String raw, String fallback) {
        if (raw == null) return fallback;
        String s = raw.replaceAll("[\\\\/:*?\"<>|]", "")
                      .replaceAll("[\\x00-\\x1F]", "")
                      .replaceAll("\\s+", " ")
                      .trim();
        if (s.length() > MAX_NAME_LEN) s = s.substring(0, MAX_NAME_LEN).trim();
        return s.isEmpty() ? fallback : s;
    }
}
