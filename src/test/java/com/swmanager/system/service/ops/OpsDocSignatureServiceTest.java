package com.swmanager.system.service.ops;

import com.swmanager.system.domain.ops.OpsDocument;
import com.swmanager.system.domain.ops.OpsDocumentSignature;
import com.swmanager.system.repository.ops.OpsDocumentRepository;
import com.swmanager.system.repository.ops.OpsDocumentSignatureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OpsDocSignatureService 단위테스트 (커버리지 상향 beyond-A, mock 기반·운영DB 무관).
 * 서명은 파일이 아닌 DB(Base64 TEXT) 저장. 생성자 주입이라 직접 생성.
 */
class OpsDocSignatureServiceTest {

    private final OpsDocumentSignatureRepository signatureRepository = mock(OpsDocumentSignatureRepository.class);
    private final OpsDocumentRepository opsDocumentRepository = mock(OpsDocumentRepository.class);

    private OpsDocSignatureService service;

    @BeforeEach
    void setUp() {
        service = new OpsDocSignatureService(signatureRepository, opsDocumentRepository);
        when(signatureRepository.save(any(OpsDocumentSignature.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void save_docNotFound_throws() {
        when(opsDocumentRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.save(9L, "CLIENT", "홍길동", "강원도청", "data:image/png;base64,AAA"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void save_setsFieldsAndSignedAt() {
        OpsDocument doc = new OpsDocument();
        when(opsDocumentRepository.findById(1L)).thenReturn(Optional.of(doc));

        OpsDocumentSignature out = service.save(1L, "CLIENT", "홍길동", "강원도청", "base64img");

        ArgumentCaptor<OpsDocumentSignature> cap = ArgumentCaptor.forClass(OpsDocumentSignature.class);
        verify(signatureRepository).save(cap.capture());
        OpsDocumentSignature saved = cap.getValue();
        assertThat(saved).isSameAs(out);
        assertThat(saved.getDocument()).isSameAs(doc);
        assertThat(saved.getSignerType()).isEqualTo("CLIENT");
        assertThat(saved.getSignerName()).isEqualTo("홍길동");
        assertThat(saved.getSignerOrg()).isEqualTo("강원도청");
        assertThat(saved.getSignatureImage()).isEqualTo("base64img");
        assertThat(saved.getSignedAt()).isNotNull();
    }

    @Test
    void findByDocId_delegates() {
        List<OpsDocumentSignature> list = List.of(new OpsDocumentSignature());
        when(signatureRepository.findByDocument_DocIdOrderByCreatedAtAsc(3L)).thenReturn(list);
        assertThat(service.findByDocId(3L)).isSameAs(list);
    }

    @Test
    void delete_delegatesToRepo() {
        service.delete(7L);
        verify(signatureRepository).deleteById(7L);
    }
}
