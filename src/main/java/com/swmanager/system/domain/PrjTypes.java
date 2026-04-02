package com.swmanager.system.domain;
import jakarta.persistence.*;

@Entity
@Table(name = "prj_types")
public class PrjTypes {
    @Id @Column(name = "cd") private String cd;
    @Column(name = "nm") private String nm;
    public PrjTypes() {}
    public String getCd() { return cd; }
    public void setCd(String cd) { this.cd = cd; }
    public String getNm() { return nm; }
    public void setNm(String nm) { this.nm = nm; }
}