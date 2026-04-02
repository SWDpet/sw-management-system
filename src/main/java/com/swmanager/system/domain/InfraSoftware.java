package com.swmanager.system.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_infra_software")
public class InfraSoftware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sw_id")
    private Long swId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    private InfraServer server;

    @Column(name = "sw_category") private String swCategory;
    @Column(name = "sw_nm") private String swNm;
    @Column(name = "sw_ver") private String swVer;
    @Column(name = "port") private String port;
    @Column(name = "sw_acc_id") private String swAccId;
    @Column(name = "sw_acc_pw") private String swAccPw;
    @Column(name = "sid") private String sid;
    @Column(name = "install_path") private String installPath;

    // Getter & Setter
    public Long getSwId() { return swId; }
    public void setSwId(Long swId) { this.swId = swId; }
    public InfraServer getServer() { return server; }
    public void setServer(InfraServer server) { this.server = server; }
    public String getSwCategory() { return swCategory; }
    public void setSwCategory(String swCategory) { this.swCategory = swCategory; }
    public String getSwNm() { return swNm; }
    public void setSwNm(String swNm) { this.swNm = swNm; }
    public String getSwVer() { return swVer; }
    public void setSwVer(String swVer) { this.swVer = swVer; }
    public String getPort() { return port; }
    public void setPort(String port) { this.port = port; }
    public String getSwAccId() { return swAccId; }
    public void setSwAccId(String swAccId) { this.swAccId = swAccId; }
    public String getSwAccPw() { return swAccPw; }
    public void setSwAccPw(String swAccPw) { this.swAccPw = swAccPw; }
    public String getSid() { return sid; }
    public void setSid(String sid) { this.sid = sid; }
    public String getInstallPath() { return installPath; }
    public void setInstallPath(String installPath) { this.installPath = installPath; }
}