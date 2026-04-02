package com.swmanager.system.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_logs")
@Data
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "userid", nullable = false)
    private String userid;

    @Column(name = "username")
    private String username;

    @Column(name = "ip_addr")
    private String ipAddr;

    // DB에 DEFAULT NOW()가 설정되어 있어도 JPA 저장 시 null이면 에러가 날 수 있으므로
    // 자바에서 저장 직전에 시간을 넣어줍니다.
    @Column(name = "access_time")
    private LocalDateTime accessTime;

    @Column(name = "menu_nm")
    private String menuNm;

    @Column(name = "action_type")
    private String actionType;

    @Column(name = "action_detail", columnDefinition = "TEXT")
    private String actionDetail;

    @PrePersist
    public void prePersist() {
        if (this.accessTime == null) {
            this.accessTime = LocalDateTime.now();
        }
    }
}