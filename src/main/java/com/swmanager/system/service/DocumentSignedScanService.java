package com.swmanager.system.service;

import com.swmanager.system.constant.enums.DocumentType;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.Document;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.workplan.DocumentRepository;

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
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Set;

/**
 * 사업문서(착수/기성/준공) 최종 도장 날인본 스캔 PDF 전용 저장 서비스.
 * 기획서/개발계획서: doc-signed-scan-upload. (codex 1차 구현검토 반영 v2)
 *
 * 범용 첨부(DocumentAttachmentService)와 분리 — 1:1 교체, COMPLETED 게이트(FR-8),
 * PDF 검증(FR-2), 사업->문서종류 폴더 트리, 경로 안전(FR-9), 원자적 교체+롤백(FR-10).
 */
@Service
@Transactional
public class DocumentSignedScanService {

    private static final Set<DocumentType> ALLOWED_TYPES =
            EnumSet.of(DocumentType.COMMENCE, DocumentType.INTERIM, DocumentType.COMPLETION);
    private static final byte[] PDF_MAGIC = { 0x25, 0x50, 0x44, 0x46, 0x2D }; // %PDF-
    private static final int MAX_NAME_LEN = 80;
    private static final long MAX_BYTES = 30L * 1024 * 1024; // 30MB
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Value("${file.scan-dir:./uploads/scan}")
    private String scanDir;

    public DocumentSignedScanService(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    private Path baseDir() {
        return Paths.get(scanDir).toAbsolutePath().normalize();
    }

    private void requireAllowed(Document doc) {
        // 착수/기성/준공(사업문서 3종)만 대상. (DocumentType enum 이 3종뿐이라 방어적.)
        if (!ALLOWED_TYPES.contains(doc.getDocType()))
            throw new IllegalStateException("착수/기성/준공 문서만 날인본을 다룰 수 있습니다.");
        // COMPLETED 게이트 제거(2026-06-09 사용자 결정): 사업문서엔 완료전환 UI가 없어 상태 무관 허용.
        // (실무상 종이 날인본이 준비되면 올리는 흐름 — 시스템 status 와 무관.)
    }

    /**
     * 업로드(또는 교체). 게이트(FR-8)·PDF검증(FR-2)·경로안전(FR-9)·원자적 교체+롤백(FR-10).
     * rollbackFor=Exception: IOException(검사예외) 발생 시에도 DB 롤백되도록.
     */
    @Transactional(rollbackFor = Exception.class)
    public Document uploadOrReplace(Integer docId, MultipartFile file, Long uploaderSeq) throws IOException {
        Document doc = documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + docId));

        // FR-8 서버측 게이트
        requireAllowed(doc);
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        if (file.getSize() > MAX_BYTES)
            throw new IllegalArgumentException("파일이 너무 큽니다(최대 30MB).");
        // FR-2 contentType 보조 검사. null(미상)은 매직바이트(%PDF) 로 충분히 걸러지므로 허용.
        //  명시적 비-PDF MIME(image/* 등)만 조기 차단. 최종 1차 기준은 아래 매직바이트.
        String ct = file.getContentType();
        if (ct != null && !ct.equalsIgnoreCase("application/pdf") && !ct.equalsIgnoreCase("application/octet-stream"))
            throw new IllegalArgumentException("PDF 파일만 허용됩니다.");

        Path base = baseDir();
        Path dir = base.resolve(projectFolder(doc)).resolve(label(doc.getDocType())).normalize();
        if (!dir.startsWith(base)) throw new SecurityException("잘못된 저장 경로입니다.");
        Files.createDirectories(dir);

        String fileName = label(doc.getDocType()) + "_" + sanitize(projNm(doc)) + "_"
                + LocalDate.now().format(DAY) + ".pdf";
        Path target = dir.resolve(fileName).normalize();
        if (!target.startsWith(base)) throw new SecurityException("잘못된 파일 경로입니다.");

        // FR-2 스트리밍 temp 작성 + 선두 매직바이트 검증 (전체 메모리 로딩 회피)
        Path temp = Files.createTempFile(dir, ".upload-", ".part");
        try {
            try (InputStream in = new BufferedInputStream(file.getInputStream())) {
                in.mark(16);
                byte[] head = in.readNBytes(PDF_MAGIC.length);
                if (!isPdf(head)) throw new IllegalArgumentException("PDF 파일만 허용됩니다.");
                in.reset();
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            }

            // FR-10 ① DB 를 먼저 flush — DB 제약 실패 시 실파일 미변경
            String prev = doc.getSignedScanPath();
            doc.setSignedScanPath(target.toString());
            doc.setSignedScanOrigName(file.getOriginalFilename());
            doc.setSignedScanSize(file.getSize());
            doc.setSignedScanUploadedAt(LocalDateTime.now());
            if (uploaderSeq != null) {
                User u = userRepository.findById(uploaderSeq).orElse(null);
                doc.setSignedScanUploadedBy(u);
            }
            documentRepository.saveAndFlush(doc);

            // FR-10 ② 같은 경로 덮어쓰기 대비 기존 target 백업
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
                if (backup != null) Files.deleteIfExists(backup); // 성공 → 백업 제거
            } catch (IOException moveErr) {
                if (backup != null) { // 실패 → 원본 복구
                    try { Files.move(backup, target, StandardCopyOption.REPLACE_EXISTING); } catch (IOException ignored) {}
                }
                throw moveErr; // rollbackFor=Exception → DB 되돌림
            }

            // 이전(다른 경로) 파일 삭제 — 1:1 정책(고아 방지)
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

    /** 날인본 삭제 (파일 + 메타). FR-8 게이트 동일 적용. */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer docId) {
        Document doc = documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + docId));
        requireAllowed(doc);
        String prev = doc.getSignedScanPath();
        if (prev != null) {
            Path base = baseDir();
            Path p = Paths.get(prev).normalize();
            if (p.startsWith(base)) { // base 밖이면 파일은 손대지 않고 메타만 정리(안전)
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            }
        }
        doc.setSignedScanPath(null);
        doc.setSignedScanOrigName(null);
        doc.setSignedScanSize(null);
        doc.setSignedScanUploadedAt(null);
        doc.setSignedScanUploadedBy(null);
        documentRepository.save(doc);
    }

    /** 다운로드용 리소스 (경로 안전 + symlink 실경로 재검증 FR-9). */
    @Transactional(readOnly = true)
    public Resource loadForDownload(Integer docId) {
        Document doc = documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + docId));
        String path = doc.getSignedScanPath();
        if (path == null) throw new IllegalArgumentException("날인본이 없습니다.");
        Path base = baseDir();
        Path p = Paths.get(path).normalize();
        if (!p.startsWith(base)) throw new SecurityException("잘못된 파일 경로입니다.");
        if (!Files.exists(p)) throw new IllegalArgumentException("파일이 존재하지 않습니다.");
        try {
            // symlink/실경로 우회 차단
            if (!p.toRealPath().startsWith(base.toRealPath())) throw new SecurityException("잘못된 파일 경로입니다.");
        } catch (IOException e) {
            throw new IllegalArgumentException("파일 접근 오류");
        }
        return new FileSystemResource(p);
    }

    @Transactional(readOnly = true)
    public String originalName(Integer docId) {
        return documentRepository.findById(docId)
                .map(Document::getSignedScanOrigName)
                .orElse("signed-scan.pdf");
    }

    // === helpers ===

    static boolean isPdf(byte[] bytes) {
        if (bytes.length < PDF_MAGIC.length) return false;
        for (int i = 0; i < PDF_MAGIC.length; i++) {
            if (bytes[i] != PDF_MAGIC[i]) return false;
        }
        return true;
    }

    private static String projNm(Document doc) {
        return (doc.getProject() != null) ? doc.getProject().getProjNm() : null;
    }

    private static String projectFolder(Document doc) {
        Long projId = (doc.getProject() != null) ? doc.getProject().getProjId() : null;
        return (projId != null ? projId : 0L) + "_" + sanitize(projNm(doc));
    }

    /** 경로 금지문자·제어문자 제거 + 공백압축 + 길이컷(80) + 빈문자 fallback. */
    static String sanitize(String raw) {
        if (raw == null) return "사업미상";
        String s = raw.replaceAll("[\\\\/:*?\"<>|]", "")  // 경로 금지문자
                      .replaceAll("[\\x00-\\x1F]", "")       // 제어문자
                      .replaceAll("\\s+", " ")
                      .trim();
        if (s.length() > MAX_NAME_LEN) s = s.substring(0, MAX_NAME_LEN).trim();
        return s.isEmpty() ? "사업미상" : s;
    }

    static String label(DocumentType type) {
        return switch (type) {
            case COMMENCE -> "착수계";
            case INTERIM -> "기성계";
            case COMPLETION -> "준공계";
            default -> "기타";
        };
    }
}
