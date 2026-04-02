package com.swmanager.system.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_infra_server")
public class InfraServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "server_id")
    private Long serverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "infra_id")
    private Infra infra;

    @Column(name = "server_type") private String serverType; // "WEB" or "DB"
    @Column(name = "ip_addr") private String ipAddr;
    @Column(name = "acc_id") private String accId;
    @Column(name = "acc_pw") private String accPw;
    @Column(name = "os_nm") private String osNm;       
    @Column(name = "mac_addr") private String macAddr;

    // === H/W 스펙 확장 필드 (업무계획 및 성과 전산화) ===
    @Column(name = "server_model", length = 200) private String serverModel;
    @Column(name = "serial_no", length = 100) private String serialNo;
    @Column(name = "cpu_spec", length = 200) private String cpuSpec;
    @Column(name = "memory_spec", length = 100) private String memorySpec;
    @Column(name = "disk_spec", length = 300) private String diskSpec;
    @Column(name = "network_spec", length = 200) private String networkSpec;
    @Column(name = "power_spec", length = 100) private String powerSpec;
    @Column(name = "os_detail", length = 300) private String osDetail;
    @Column(name = "rack_location", length = 100) private String rackLocation;
    @Column(name = "note", columnDefinition = "TEXT") private String note;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InfraSoftware> softwares = new ArrayList<>();

    // === 기존 Getter & Setter ===
    public Long getServerId() { return serverId; }
    public void setServerId(Long serverId) { this.serverId = serverId; }
    public Infra getInfra() { return infra; }
    public void setInfra(Infra infra) { this.infra = infra; }
    public String getServerType() { return serverType; }
    public void setServerType(String serverType) { this.serverType = serverType; }
    public String getIpAddr() { return ipAddr; }
    public void setIpAddr(String ipAddr) { this.ipAddr = ipAddr; }
    public String getAccId() { return accId; }
    public void setAccId(String accId) { this.accId = accId; }
    public String getAccPw() { return accPw; }
    public void setAccPw(String accPw) { this.accPw = accPw; }
    public String getOsNm() { return osNm; }
    public void setOsNm(String osNm) { this.osNm = osNm; }
    public String getMacAddr() { return macAddr; }
    public void setMacAddr(String macAddr) { this.macAddr = macAddr; }
    public List<InfraSoftware> getSoftwares() { return softwares; }
    public void setSoftwares(List<InfraSoftware> softwares) { this.softwares = softwares; }

    // === H/W 스펙 Getter & Setter ===
    public String getServerModel() { return serverModel; }
    public void setServerModel(String serverModel) { this.serverModel = serverModel; }
    public String getSerialNo() { return serialNo; }
    public void setSerialNo(String serialNo) { this.serialNo = serialNo; }
    public String getCpuSpec() { return cpuSpec; }
    public void setCpuSpec(String cpuSpec) { this.cpuSpec = cpuSpec; }
    public String getMemorySpec() { return memorySpec; }
    public void setMemorySpec(String memorySpec) { this.memorySpec = memorySpec; }
    public String getDiskSpec() { return diskSpec; }
    public void setDiskSpec(String diskSpec) { this.diskSpec = diskSpec; }
    public String getNetworkSpec() { return networkSpec; }
    public void setNetworkSpec(String networkSpec) { this.networkSpec = networkSpec; }
    public String getPowerSpec() { return powerSpec; }
    public void setPowerSpec(String powerSpec) { this.powerSpec = powerSpec; }
    public String getOsDetail() { return osDetail; }
    public void setOsDetail(String osDetail) { this.osDetail = osDetail; }
    public String getRackLocation() { return rackLocation; }
    public void setRackLocation(String rackLocation) { this.rackLocation = rackLocation; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}