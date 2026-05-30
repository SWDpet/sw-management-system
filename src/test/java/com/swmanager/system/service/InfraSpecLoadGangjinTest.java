package com.swmanager.system.service;

import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.InfraServer;
import com.swmanager.system.repository.InfraRepository;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 일회성 데이터 적재 — 강진군 UPIS 점검내역서(HWPX)에서 추출한 DB/AP 서버 H/W 사양을
 * tb_infra_server 의 비어 있는 HW 필드에 채운다.
 *
 * <p><b>DRY-RUN 모드</b>: @Commit 이 없으면 @Transactional 이 롤백 → 변경 미반영(현재상태+변경안만 출력).
 * 실제 적재 시 @Commit 활성화.
 */
@SpringBootTest
@ActiveProfiles("local")
class InfraSpecLoadGangjinTest {

    @Autowired private InfraRepository infraRepository;

    @Test
    @Transactional
    // @Commit   // 강진군 적재 완료(2026-05-30, QR 실측 정정 + AIX 6.1/Oracle 11g 확정). 재적재 시 주석 해제.
    void loadGangjinServerSpecs() {
        List<Infra> infras = infraRepository.findByDistNmAndSysNmEn("강진군", "UPIS");
        Assumptions.assumeFalse(infras.isEmpty(), "강진군 UPIS 인프라 레코드 없음");

        int servers = 0;
        for (Infra infra : infras) {
            System.out.println("[INFRA] id=" + infra.getInfraId() + " dist=" + infra.getDistNm()
                    + " sysEn=" + infra.getSysNmEn() + " servers=" + infra.getServers().size());
            for (InfraServer s : infra.getServers()) {
                boolean isDb = "DB".equalsIgnoreCase(s.getServerType());
                System.out.println("  [BEFORE] serverId=" + s.getServerId() + " type=" + s.getServerType()
                        + " model=" + s.getServerModel() + " cpu=" + s.getCpuSpec() + " mem=" + s.getMemorySpec()
                        + " disk=" + s.getDiskSpec() + " net=" + s.getNetworkSpec() + " power=" + s.getPowerSpec()
                        + " osNm=" + s.getOsNm() + " osDetail=" + s.getOsDetail());

                if (isDb) {
                    s.setServerModel("IBM P720 (8202-E4D)");
                    s.setSerialNo("06D535T");
                    s.setCpuSpec("PowerPC POWER7 3.6GHz * 4Core");
                    s.setMemorySpec("32GB");                                  // QR 실측 정정 (HWP 64GB → 실측 31,488MB ≈ 32GB)
                    s.setDiskSpec("146GB SAS 10Krpm * 8EA (내부디스크, 운영체제)");
                    s.setNetworkSpec("10/100/1000 Base-TX * 4Port");
                    s.setPowerSpec("이중화 전원");
                    s.setOsNm("AIX 6.1");                                    // 사용자 확인 (6.0 → 6.1). Oracle 11g 는 기존 인프라 software 유지
                } else {
                    s.setServerModel("IBM X3650 M4 (7915-AC1)");
                    s.setSerialNo("06AWMGG");
                    s.setCpuSpec("Intel Xeon E5-2690 v2 3.0GHz * 10Core");    // QR 실측 정정 (HWP 2.9GHz 8Core 오류)
                    s.setMemorySpec("16GB");
                    s.setDiskSpec("300GB SAS 10Krpm * 5EA (내부디스크, 운영체제)");
                    s.setNetworkSpec("10/100/1000 Base-TX * 4Port");
                    s.setPowerSpec("이중화 전원");
                    s.setOsNm("Windows Server 2012 R2 Standard");             // QR 실측 정정 (Enterprise → Standard)
                }
                System.out.println("  [AFTER ] type=" + s.getServerType() + " model=" + s.getServerModel()
                        + " cpu=" + s.getCpuSpec() + " mem=" + s.getMemorySpec() + " disk=" + s.getDiskSpec());
                servers++;
            }
        }
        System.out.println("[INFRALOAD] 강진군 서버 " + servers + "건 갱신 (HW 사양)");
    }
}
