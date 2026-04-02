package com.swmanager.system.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "sigungu_code")
public class SigunguCode {
    @Id
    @Column(name = "adm_sect_c")
    private String admSectC; // 행정구역코드 (예: 51150)

    @Column(name = "sido_name")
    private String sidoNm;   

    @Column(name = "sgg_name")
    private String sggNm;    

    @Column(name = "instt_c") // 실제 기관코드 칼럼 매핑
    private String insttC;    

    public SigunguCode() {}

    // Jackson JSON 변환기 및 화면 표시를 위해 필수적인 Getter/Setter
    public String getAdmSectC() { return admSectC; }
    public void setAdmSectC(String admSectC) { this.admSectC = admSectC; }

    public String getSidoNm() { return sidoNm; }
    public void setSidoNm(String sidoNm) { this.sidoNm = sidoNm; }

    public String getSggNm() { return sggNm; }
    public void setSggNm(String sggNm) { this.sggNm = sggNm; }

    public String getInsttC() { return insttC; }
    public void setInsttC(String insttC) { this.insttC = insttC; }
}