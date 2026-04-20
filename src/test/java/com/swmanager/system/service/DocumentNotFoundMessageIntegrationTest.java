package com.swmanager.system.service;

import com.swmanager.system.i18n.MessageResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 통합성 테스트: MessageSource + MessageResolver + DocumentService 예외 메시지 치환 end-to-end.
 * 개발계획서 v2 §1 Step 5 / codex v1 권장사항 #5-c.
 *
 * Spring 컨텍스트 로드 후 존재하지 않는 Document ID 조회 시, BusinessException 메시지가
 * messages.properties 로부터 조립된 한글 + ID 치환본이어야 함을 검증.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "logging.level.org.hibernate.SQL=OFF"
})
class DocumentNotFoundMessageIntegrationTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private MessageResolver messages;

    @Test
    void messageResolverBean_isWired() {
        assertThat(messages).isNotNull();
    }

    @Test
    void getDocumentById_nonexistentId_throwsWithResolvedKoreanMessage() {
        // 작은 ID 사용 (MessageFormat이 숫자에 locale 천단위 구분자를 넣지 않도록)
        Integer nonExistentId = 999;

        assertThatThrownBy(() -> documentService.getDocumentById(nonExistentId))
                // DocumentService는 IllegalArgumentException을 던짐 (기존 동작, 본 스프린트 범위 외)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("문서를 찾을 수 없습니다")
                .hasMessageContaining("999");
    }
}
