package com.swmanager.system.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_infra_link_upis")
public class InfraLinkUpis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long linkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "infra_id")
    private Infra infra;

    @Column(name = "kras_ip") private String krasIp;
    @Column(name = "kras_cd") private String krasCd;
    @Column(name = "gpki_id") private String gpkiId;
    @Column(name = "gpki_pw") private String gpkiPw;
    @Column(name = "minwon_ip") private String minwonIp;
    @Column(name = "minwon_link_cd") private String minwonLinkCd;
    @Column(name = "minwon_key") private String minwonKey;
    @Column(name = "doc_ip") private String docIp;
    @Column(name = "doc_adm_id") private String docAdmId;
    @Column(name = "doc_id") private String docId;

    // Getter & Setter
    public Long getLinkId() { return linkId; }
    public void setLinkId(Long linkId) { this.linkId = linkId; }
    public Infra getInfra() { return infra; }
    public void setInfra(Infra infra) { this.infra = infra; }
    public String getKrasIp() { return krasIp; }
    public void setKrasIp(String krasIp) { this.krasIp = krasIp; }
    public String getKrasCd() { return krasCd; }
    public void setKrasCd(String krasCd) { this.krasCd = krasCd; }
    public String getGpkiId() { return gpkiId; }
    public void setGpkiId(String gpkiId) { this.gpkiId = gpkiId; }
    public String getGpkiPw() { return gpkiPw; }
    public void setGpkiPw(String gpkiPw) { this.gpkiPw = gpkiPw; }
    public String getMinwonIp() { return minwonIp; }
    public void setMinwonIp(String minwonIp) { this.minwonIp = minwonIp; }
    public String getMinwonLinkCd() { return minwonLinkCd; }
    public void setMinwonLinkCd(String minwonLinkCd) { this.minwonLinkCd = minwonLinkCd; }
    public String getMinwonKey() { return minwonKey; }
    public void setMinwonKey(String minwonKey) { this.minwonKey = minwonKey; }
    public String getDocIp() { return docIp; }
    public void setDocIp(String docIp) { this.docIp = docIp; }
    public String getDocAdmId() { return docAdmId; }
    public void setDocAdmId(String docAdmId) { this.docAdmId = docAdmId; }
    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }
}