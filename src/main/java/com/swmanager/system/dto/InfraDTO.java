package com.swmanager.system.dto;

import lombok.Data;
import lombok.Builder;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 인프라 정보 DTO
 * 비밀번호, API 키 등 민감 정보는 권한에 따라 마스킹 처리
 */
@Data
@Builder
public class InfraDTO {
    
    private Long infraId;
    private String infraType; // PROD / TEST
    
    // 기본 정보
    private String cityNm;
    private String distNm;
    private String sysNm;
    private String sysNmEn;
    private String orgCd;
    private String distCd;
    
    // 서버 정보 (민감 정보 마스킹)
    private List<InfraServerDTO> servers;
    
    // 연계 정보 (민감 정보 마스킹)
    private List<InfraLinkUpisDTO> upisLinks;
    private List<InfraLinkApiDTO> apiLinks;
    
    // 메모 정보
    private List<InfraMemoDTO> memos;
    
    @Data
    @Builder
    public static class InfraServerDTO {
        private Long serverId;
        private String serverType; // WEB / DB
        private String ipAddr;
        private String accId;
        private String accPw; // 권한에 따라 마스킹: "********"
        private String osNm;
        private String macAddr;

        // H/W 스펙 정보 (I-01 화면)
        private String serverModel;   // 서버 모델명
        private String serialNo;      // 시리얼 번호
        private String cpuSpec;       // CPU 스펙
        private String memorySpec;    // 메모리 스펙
        private String diskSpec;      // 디스크 스펙
        private String networkSpec;   // 네트워크 스펙
        private String powerSpec;     // 전원 스펙
        private String osDetail;      // OS 상세 정보
        private String rackLocation;  // 랙 위치
        private String note;          // 비고

        private List<InfraSoftwareDTO> softwares;
    }
    
    @Data
    @Builder
    public static class InfraSoftwareDTO {
        private Long swId;
        private String swCategory;
        private String swNm;
        private String swVer;
        private String port;
        private String swAccId;
        private String swAccPw; // 권한에 따라 마스킹
        private String sid;
        private String installPath;
    }
    
    @Data
    @Builder
    public static class InfraLinkUpisDTO {
        private Long linkId;
        private String krasIp;
        private String krasCd;
        private String gpkiId;
        private String gpkiPw; // 마스킹
        private String minwonIp;
        private String minwonLinkCd;
        private String minwonKey; // 마스킹
        private String docIp;
        private String docAdmId;
        private String docId;
    }
    
    @Data
    @Builder
    public static class InfraLinkApiDTO {
        private Long apiId;
        
        // NAVER NEWS
        private String naverNewsKey; // 마스킹
        private String naverNewsExpDt;
        private String naverNewsUser;
        
        // NAVER SECRET
        private String naverSecretKey; // 마스킹
        private String naverSecretExpDt;
        private String naverSecretUser;
        
        // 공공데이터포털
        private String publicDataKey; // 마스킹
        private String publicDataExpDt;
        private String publicDataUser;
        
        // KRAS
        private String krasKey; // 마스킹
        private String krasExpDt;
        private String krasUser;
        
        // K-GEO
        private String kgeoKey; // 마스킹
        private String kgeoExpDt;
        private String kgeoUser;
        
        // Vworld
        private String vworldKey; // 마스킹
        private String vworldExpDt;
        private String vworldUser;
        
        // KAKAO
        private String kakaoKey; // 마스킹
        private String kakaoExpDt;
        private String kakaoUser;
    }
    
    @Data
    @Builder
    public static class InfraMemoDTO {
        private Long memoId;
        private String memoContent;
        private String memoDate;
        private String memoWriter;
    }
    
    /**
     * Entity에서 DTO로 변환 (민감 정보 마스킹 옵션 포함)
     * @param entity 인프라 엔티티
     * @param maskSensitiveData true: 민감정보 마스킹, false: 원본 표시
     */
    public static InfraDTO fromEntity(com.swmanager.system.domain.Infra entity, boolean maskSensitiveData) {
        if (entity == null) {
            return null;
        }
        
        return InfraDTO.builder()
                .infraId(entity.getInfraId())
                .infraType(entity.getInfraType())
                .cityNm(entity.getCityNm())
                .distNm(entity.getDistNm())
                .sysNm(entity.getSysNm())
                .sysNmEn(entity.getSysNmEn())
                .orgCd(entity.getOrgCd())
                .distCd(entity.getDistCd())
                .servers(entity.getServers() != null ? 
                    entity.getServers().stream()
                        .map(s -> convertServer(s, maskSensitiveData))
                        .collect(Collectors.toList()) : null)
                .upisLinks(entity.getUpisLinks() != null ?
                    entity.getUpisLinks().stream()
                        .map(u -> convertUpis(u, maskSensitiveData))
                        .collect(Collectors.toList()) : null)
                .apiLinks(entity.getApiLinks() != null ?
                    entity.getApiLinks().stream()
                        .map(a -> convertApi(a, maskSensitiveData))
                        .collect(Collectors.toList()) : null)
                .build();
    }
    
    private static InfraServerDTO convertServer(com.swmanager.system.domain.InfraServer server, boolean mask) {
        return InfraServerDTO.builder()
                .serverId(server.getServerId())
                .serverType(server.getServerType())
                .ipAddr(server.getIpAddr())
                .accId(server.getAccId())
                .accPw(mask ? "********" : server.getAccPw())
                .osNm(server.getOsNm())
                .macAddr(server.getMacAddr())
                // H/W 스펙 매핑
                .serverModel(server.getServerModel())
                .serialNo(server.getSerialNo())
                .cpuSpec(server.getCpuSpec())
                .memorySpec(server.getMemorySpec())
                .diskSpec(server.getDiskSpec())
                .networkSpec(server.getNetworkSpec())
                .powerSpec(server.getPowerSpec())
                .osDetail(server.getOsDetail())
                .rackLocation(server.getRackLocation())
                .note(server.getNote())
                .softwares(server.getSoftwares() != null ?
                    server.getSoftwares().stream()
                        .map(sw -> InfraSoftwareDTO.builder()
                            .swId(sw.getSwId())
                            .swCategory(sw.getSwCategory())
                            .swNm(sw.getSwNm())
                            .swVer(sw.getSwVer())
                            .port(sw.getPort())
                            .swAccId(sw.getSwAccId())
                            .swAccPw(mask ? "********" : sw.getSwAccPw())
                            .sid(sw.getSid())
                            .installPath(sw.getInstallPath())
                            .build())
                        .collect(Collectors.toList()) : null)
                .build();
    }
    
    private static InfraLinkUpisDTO convertUpis(com.swmanager.system.domain.InfraLinkUpis upis, boolean mask) {
        return InfraLinkUpisDTO.builder()
                .linkId(upis.getLinkId())
                .krasIp(upis.getKrasIp())
                .krasCd(upis.getKrasCd())
                .gpkiId(upis.getGpkiId())
                .gpkiPw(mask ? "********" : upis.getGpkiPw())
                .minwonIp(upis.getMinwonIp())
                .minwonLinkCd(upis.getMinwonLinkCd())
                .minwonKey(mask ? "********" : upis.getMinwonKey())
                .docIp(upis.getDocIp())
                .docAdmId(upis.getDocAdmId())
                .docId(upis.getDocId())
                .build();
    }
    
    private static InfraLinkApiDTO convertApi(com.swmanager.system.domain.InfraLinkApi api, boolean mask) {
        return InfraLinkApiDTO.builder()
                .apiId(api.getApiId())
                .naverNewsKey(mask ? maskKey(api.getNaverNewsKey()) : api.getNaverNewsKey())
                .naverNewsExpDt(api.getNaverNewsExpDt() != null ? api.getNaverNewsExpDt().toString() : null)
                .naverNewsUser(api.getNaverNewsUser())
                .naverSecretKey(mask ? maskKey(api.getNaverSecretKey()) : api.getNaverSecretKey())
                .naverSecretExpDt(api.getNaverSecretExpDt() != null ? api.getNaverSecretExpDt().toString() : null)
                .naverSecretUser(api.getNaverSecretUser())
                .publicDataKey(mask ? maskKey(api.getPublicDataKey()) : api.getPublicDataKey())
                .publicDataExpDt(api.getPublicDataExpDt() != null ? api.getPublicDataExpDt().toString() : null)
                .publicDataUser(api.getPublicDataUser())
                .krasKey(mask ? maskKey(api.getKrasKey()) : api.getKrasKey())
                .krasExpDt(api.getKrasExpDt() != null ? api.getKrasExpDt().toString() : null)
                .krasUser(api.getKrasUser())
                .kgeoKey(mask ? maskKey(api.getKgeoKey()) : api.getKgeoKey())
                .kgeoExpDt(api.getKgeoExpDt() != null ? api.getKgeoExpDt().toString() : null)
                .kgeoUser(api.getKgeoUser())
                .vworldKey(mask ? maskKey(api.getVworldKey()) : api.getVworldKey())
                .vworldExpDt(api.getVworldExpDt() != null ? api.getVworldExpDt().toString() : null)
                .vworldUser(api.getVworldUser())
                .kakaoKey(mask ? maskKey(api.getKakaoKey()) : api.getKakaoKey())
                .kakaoExpDt(api.getKakaoExpDt() != null ? api.getKakaoExpDt().toString() : null)
                .kakaoUser(api.getKakaoUser())
                .build();
    }
    
    /**
     * API 키 마스킹 (앞 4자리만 표시)
     */
    private static String maskKey(String key) {
        if (key == null || key.length() <= 4) {
            return "****";
        }
        return key.substring(0, 4) + "****" + "****";
    }
}
