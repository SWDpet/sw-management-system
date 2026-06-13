package com.swmanager.system.domain.ops;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * KB 추천 채택 피드백 (tb_ops_kb_feedback) — ops-fault-support M3/P5.
 * 추천 '적용'/'무시' 이벤트 적재 → 후속 랭킹·학습 근거. 익명 집계(작성자 미추적).
 */
@Entity
@Table(name = "tb_ops_kb_feedback")
@Getter @Setter
public class OpsKbFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @Column(name = "kb_id", nullable = false)
    private Long kbId;

    @Column(name = "doc_id")
    private Long docId;

    @Column(name = "fb_action", length = 20)
    private String fbAction;   // APPLIED / IGNORED

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}
