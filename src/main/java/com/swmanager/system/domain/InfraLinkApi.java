package com.swmanager.system.domain;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Entity
@Table(name = "tb_infra_link_api")
public class InfraLinkApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "api_id")
    private Long apiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "infra_id")
    private Infra infra;

    // --- NAVER NEWS ---
    @Column(name = "naver_news_key") private String naverNewsKey;
    @DateTimeFormat(pattern = "yyyy-MM-dd") @Column(name = "naver_news_req_dt") private LocalDate naverNewsReqDt;
    @DateTimeFormat(pattern = "yyyy-MM-dd") @Column(name = "naver_news_exp_dt") private LocalDate naverNewsExpDt;
    @Column(name = "naver_news_user") private String naverNewsUser;

    // --- NAVER SECRET ---
    @Column(name = "naver_secret_key") private String naverSecretKey;
    @DateTimeFormat(pattern = "yyyy-MM-dd") @Column(name = "naver_secret_req_dt") private LocalDate naverSecretReqDt;
    @DateTimeFormat(pattern = "yyyy-MM-dd") @Column(name = "naver_secret_exp_dt") private LocalDate naverSecretExpDt;
    @Column(name = "naver_secret_user") private String naverSecretUser;

    // --- 공공데이터포털 ---
    @Column(name = "public_data_key") private String publicDataKey;
    @DateTimeFormat(pattern = "yyyy-MM-dd") @Column(name = "public_data_req_dt") private LocalDate publicDataReqDt;
    @DateTimeFormat(pattern = "yyyy-MM-dd") @Column(name = "public_data_exp_dt") private LocalDate publicDataExpDt;
    @Column(name = "public_data_user") private String publicDataUser;

    // --- KRAS ---
    @Column(name = "kras_key") private String krasKey;
    @DateTimeFormat(pattern = "yyyy-MM-dd") @Column(name = "kras_req_dt") private LocalDate krasReqDt;
    @DateTimeFormat(pattern = "yyyy-MM-dd") @Column(name = "kras_exp_dt") private LocalDate krasExpDt;
    @Column(name = "kras_user") private String krasUser;

    // --- K-GEO ---
    @Column(name = "kgeo_key") private String kgeoKey;
    @DateTimeFormat(pattern = "yyyy-MM-dd") @Column(name = "kgeo_req_dt") private LocalDate kgeoReqDt;
    @DateTimeFormat(pattern = "yyyy-MM-dd") @Column(name = "kgeo_exp_dt") private LocalDate kgeoExpDt;
    @Column(name = "kgeo_user") private String kgeoUser;

    // --- Vworld ---
    @Column(name = "vworld_key") private String vworldKey;
    @DateTimeFormat(pattern = "yyyy-MM-dd") @Column(name = "vworld_req_dt") private LocalDate vworldReqDt;
    @DateTimeFormat(pattern = "yyyy-MM-dd") @Column(name = "vworld_exp_dt") private LocalDate vworldExpDt;
    @Column(name = "vworld_user") private String vworldUser;

    // --- [추가됨] KAKAO ---
    @Column(name = "kakao_key") private String kakaoKey;
    @DateTimeFormat(pattern = "yyyy-MM-dd") @Column(name = "kakao_req_dt") private LocalDate kakaoReqDt;
    @DateTimeFormat(pattern = "yyyy-MM-dd") @Column(name = "kakao_exp_dt") private LocalDate kakaoExpDt;
    @Column(name = "kakao_user") private String kakaoUser;


    // Getter & Setter
    public Long getApiId() { return apiId; }
    public void setApiId(Long apiId) { this.apiId = apiId; }
    public Infra getInfra() { return infra; }
    public void setInfra(Infra infra) { this.infra = infra; }

    public String getNaverNewsKey() { return naverNewsKey; }
    public void setNaverNewsKey(String naverNewsKey) { this.naverNewsKey = naverNewsKey; }
    public LocalDate getNaverNewsReqDt() { return naverNewsReqDt; }
    public void setNaverNewsReqDt(LocalDate naverNewsReqDt) { this.naverNewsReqDt = naverNewsReqDt; }
    public LocalDate getNaverNewsExpDt() { return naverNewsExpDt; }
    public void setNaverNewsExpDt(LocalDate naverNewsExpDt) { this.naverNewsExpDt = naverNewsExpDt; }
    public String getNaverNewsUser() { return naverNewsUser; }
    public void setNaverNewsUser(String naverNewsUser) { this.naverNewsUser = naverNewsUser; }

    public String getNaverSecretKey() { return naverSecretKey; }
    public void setNaverSecretKey(String naverSecretKey) { this.naverSecretKey = naverSecretKey; }
    public LocalDate getNaverSecretReqDt() { return naverSecretReqDt; }
    public void setNaverSecretReqDt(LocalDate naverSecretReqDt) { this.naverSecretReqDt = naverSecretReqDt; }
    public LocalDate getNaverSecretExpDt() { return naverSecretExpDt; }
    public void setNaverSecretExpDt(LocalDate naverSecretExpDt) { this.naverSecretExpDt = naverSecretExpDt; }
    public String getNaverSecretUser() { return naverSecretUser; }
    public void setNaverSecretUser(String naverSecretUser) { this.naverSecretUser = naverSecretUser; }

    public String getPublicDataKey() { return publicDataKey; }
    public void setPublicDataKey(String publicDataKey) { this.publicDataKey = publicDataKey; }
    public LocalDate getPublicDataReqDt() { return publicDataReqDt; }
    public void setPublicDataReqDt(LocalDate publicDataReqDt) { this.publicDataReqDt = publicDataReqDt; }
    public LocalDate getPublicDataExpDt() { return publicDataExpDt; }
    public void setPublicDataExpDt(LocalDate publicDataExpDt) { this.publicDataExpDt = publicDataExpDt; }
    public String getPublicDataUser() { return publicDataUser; }
    public void setPublicDataUser(String publicDataUser) { this.publicDataUser = publicDataUser; }

    public String getKrasKey() { return krasKey; }
    public void setKrasKey(String krasKey) { this.krasKey = krasKey; }
    public LocalDate getKrasReqDt() { return krasReqDt; }
    public void setKrasReqDt(LocalDate krasReqDt) { this.krasReqDt = krasReqDt; }
    public LocalDate getKrasExpDt() { return krasExpDt; }
    public void setKrasExpDt(LocalDate krasExpDt) { this.krasExpDt = krasExpDt; }
    public String getKrasUser() { return krasUser; }
    public void setKrasUser(String krasUser) { this.krasUser = krasUser; }

    public String getKgeoKey() { return kgeoKey; }
    public void setKgeoKey(String kgeoKey) { this.kgeoKey = kgeoKey; }
    public LocalDate getKgeoReqDt() { return kgeoReqDt; }
    public void setKgeoReqDt(LocalDate kgeoReqDt) { this.kgeoReqDt = kgeoReqDt; }
    public LocalDate getKgeoExpDt() { return kgeoExpDt; }
    public void setKgeoExpDt(LocalDate kgeoExpDt) { this.kgeoExpDt = kgeoExpDt; }
    public String getKgeoUser() { return kgeoUser; }
    public void setKgeoUser(String kgeoUser) { this.kgeoUser = kgeoUser; }

    public String getVworldKey() { return vworldKey; }
    public void setVworldKey(String vworldKey) { this.vworldKey = vworldKey; }
    public LocalDate getVworldReqDt() { return vworldReqDt; }
    public void setVworldReqDt(LocalDate vworldReqDt) { this.vworldReqDt = vworldReqDt; }
    public LocalDate getVworldExpDt() { return vworldExpDt; }
    public void setVworldExpDt(LocalDate vworldExpDt) { this.vworldExpDt = vworldExpDt; }
    public String getVworldUser() { return vworldUser; }
    public void setVworldUser(String vworldUser) { this.vworldUser = vworldUser; }

    public String getKakaoKey() { return kakaoKey; }
    public void setKakaoKey(String kakaoKey) { this.kakaoKey = kakaoKey; }
    public LocalDate getKakaoReqDt() { return kakaoReqDt; }
    public void setKakaoReqDt(LocalDate kakaoReqDt) { this.kakaoReqDt = kakaoReqDt; }
    public LocalDate getKakaoExpDt() { return kakaoExpDt; }
    public void setKakaoExpDt(LocalDate kakaoExpDt) { this.kakaoExpDt = kakaoExpDt; }
    public String getKakaoUser() { return kakaoUser; }
    public void setKakaoUser(String kakaoUser) { this.kakaoUser = kakaoUser; }
}