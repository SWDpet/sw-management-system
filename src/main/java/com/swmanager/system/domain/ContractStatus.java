package com.swmanager.system.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 계약 상태 마스터 테이블 매핑 클래스
 */
@Entity
@Table(name = "cont_stat_mst")
public class ContractStatus {

    @Id
    private String cd; // 상태 코드 (PK)
    private String nm; // 상태 명칭

    // --- Thymeleaf 표출을 위한 Getter / Setter 메서드 직접 작성 ---
    public String getCd() {
        return cd;
    }

    public void setCd(String cd) {
        this.cd = cd;
    }

    public String getNm() {
        // HTML의 p.status.nm 호출 시 이 메서드가 실행됩니다.
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }
}