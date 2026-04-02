package com.swmanager.system.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ps_info")
public class PersonInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sys_nm_en") 
    private String sysNmEn;
    
    @Column(name = "sys_nm") 
    private String sysNm;

    @Column(name = "city_nm") 
    private String cityNm;

    @Column(name = "org_cd") 
    private String orgCd;

    @Column(name = "dist_nm") 
    private String distNm;

    @Column(name = "dist_cd") 
    private String distCd;

    @Column(name = "org_nm") 
    private String orgNm;

    @Column(name = "dept_nm") 
    private String deptNm;

    @Column(name = "team_nm") 
    private String teamNm;

    // [수정] 복잡한 매핑 없이 DB 컬럼 'pos'를 그대로 사용합니다.
    @Column(name = "pos") 
    private String pos;

    @Column(name = "user_nm") 
    private String userNm;

    @Column(name = "tel") 
    private String tel;

    @Column(name = "email") 
    private String email;

    @Column(name = "reg_dt") 
    private LocalDateTime regDt;

    @PrePersist
    public void prePersist() {
        this.regDt = LocalDateTime.now();
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSysNmEn() { return sysNmEn; }
    public void setSysNmEn(String sysNmEn) { this.sysNmEn = sysNmEn; }
    
    public String getSysNm() { return sysNm; }
    public void setSysNm(String sysNm) { this.sysNm = sysNm; }
    
    public String getCityNm() { return cityNm; }
    public void setCityNm(String cityNm) { this.cityNm = cityNm; }
    
    public String getOrgCd() { return orgCd; }
    public void setOrgCd(String orgCd) { this.orgCd = orgCd; }
    
    public String getDistNm() { return distNm; }
    public void setDistNm(String distNm) { this.distNm = distNm; }
    
    public String getDistCd() { return distCd; }
    public void setDistCd(String distCd) { this.distCd = distCd; }
    
    public String getOrgNm() { return orgNm; }
    public void setOrgNm(String orgNm) { this.orgNm = orgNm; }
    
    public String getDeptNm() { return deptNm; }
    public void setDeptNm(String deptNm) { this.deptNm = deptNm; }
    
    public String getTeamNm() { return teamNm; }
    public void setTeamNm(String teamNm) { this.teamNm = teamNm; }
    
    // [수정] pos Getter/Setter (rankNm 제거됨)
    public String getPos() { return pos; }
    public void setPos(String pos) { this.pos = pos; }
    
    public String getUserNm() { return userNm; }
    public void setUserNm(String userNm) { this.userNm = userNm; }
    
    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getRegDt() { return regDt; }
    public void setRegDt(LocalDateTime regDt) { this.regDt = regDt; }
}