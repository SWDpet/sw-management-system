package com.swmanager.system.service.ops;

import com.swmanager.system.domain.ops.OpsDocument;
import com.swmanager.system.domain.ops.OpsDocumentSignature;
import com.swmanager.system.repository.ops.OpsDocumentRepository;
import com.swmanager.system.repository.ops.OpsDocumentSignatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 운영·유지보수 문서 서명 서비스. signature_image TEXT (Base64 PNG).
 */
@Service
@Transactional
@RequiredArgsConstructor
public class OpsDocSignatureService {

    private final OpsDocumentSignatureRepository signatureRepository;
    private final OpsDocumentRepository opsDocumentRepository;

    public OpsDocumentSignature save(Long docId, String signerType, String signerName, String signerOrg, String signatureImage) {
        OpsDocument doc = opsDocumentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("ops_doc not found: " + docId));
        OpsDocumentSignature sig = new OpsDocumentSignature();
        sig.setDocument(doc);
        sig.setSignerType(signerType);
        sig.setSignerName(signerName);
        sig.setSignerOrg(signerOrg);
        sig.setSignatureImage(signatureImage);
        sig.setSignedAt(LocalDateTime.now());
        return signatureRepository.save(sig);
    }

    @Transactional(readOnly = true)
    public List<OpsDocumentSignature> findByDocId(Long docId) {
        return signatureRepository.findByDocument_DocIdOrderByCreatedAtAsc(docId);
    }

    public void delete(Long signId) {
        signatureRepository.deleteById(signId);
    }
}
