package com.swmanager.system.domain;
import jakarta.persistence.*;

@Entity
@Table(name = "cont_frm_mst")
public class ContFrmMst {
    @Id @Column(name = "cd") private String cd;
    @Column(name = "nm") private String nm;
    public ContFrmMst() {}
    public String getCd() { return cd; }
    public void setCd(String cd) { this.cd = cd; }
    public String getNm() { return nm; }
    public void setNm(String nm) { this.nm = nm; }
}