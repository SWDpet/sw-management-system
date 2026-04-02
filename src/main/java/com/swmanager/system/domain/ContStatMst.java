package com.swmanager.system.domain;
import jakarta.persistence.*;

@Entity
@Table(name = "cont_stat_mst")
public class ContStatMst {
    @Id @Column(name = "cd") private String cd;
    @Column(name = "nm") private String nm;
    public ContStatMst() {}
    public String getCd() { return cd; }
    public void setCd(String cd) { this.cd = cd; }
    public String getNm() { return nm; }
    public void setNm(String nm) { this.nm = nm; }
}