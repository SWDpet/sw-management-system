package com.swmanager.system.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "tb_infra_master")
public class Infra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "infra_id")
    private Long infraId;

    @Column(name = "infra_type")
    private String infraType;

    @Column(name = "city_nm") private String cityNm;
    @Column(name = "dist_nm") private String distNm;
    @Column(name = "sys_nm") private String sysNm;
    @Column(name = "sys_nm_en") private String sysNmEn;
    @Column(name = "org_cd") private String orgCd;
    @Column(name = "dist_cd") private String distCd;

    @OneToMany(mappedBy = "infra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InfraServer> servers = new ArrayList<>();

    @OneToMany(mappedBy = "infra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InfraLinkUpis> upisLinks = new ArrayList<>();

    @OneToMany(mappedBy = "infra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InfraLinkApi> apiLinks = new ArrayList<>();

    @OneToMany(mappedBy = "infra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InfraMemo> memos = new ArrayList<>();
}
