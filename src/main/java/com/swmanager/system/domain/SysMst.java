package com.swmanager.system.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "sys_mst")
public class SysMst {
    @Id 
    @Column(name = "cd") 
    private String cd;

    @Column(name = "nm") 
    private String nm;

    public SysMst() {}

    public String getCd() { return cd; }
    public void setCd(String cd) { this.cd = cd; }

    public String getNm() { return nm; }
    public void setNm(String nm) { this.nm = nm; }
}