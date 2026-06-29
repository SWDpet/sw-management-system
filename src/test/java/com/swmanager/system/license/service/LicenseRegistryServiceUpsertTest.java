package com.swmanager.system.license.service;

import com.swmanager.system.license.domain.LicenseRegistry;
import com.swmanager.system.license.repository.LicenseRegistryRepository;
import com.swmanager.system.license.repository.LicenseUploadHistoryRepository;
import com.swmanager.system.license.service.LicenseRegistryService.LicenseUpsertOutcome;
import com.swmanager.system.license.service.LicenseRegistryService.UpsertDecision;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * classifyUpsert(공유 upsert 코어) 단위테스트 (P10, T-4 / 회귀·멱등).
 * 기존 CSV 업로드 기준(중복키 License ID+Product ID, Hardware ID/License String 변경 시에만 갱신)을
 * 추출 후에도 그대로 보존하는지 검증. DB 불필요(repo mock).
 */
class LicenseRegistryServiceUpsertTest {

    private final LicenseRegistryRepository repo = mock(LicenseRegistryRepository.class);
    private final LicenseUploadHistoryRepository historyRepo = mock(LicenseUploadHistoryRepository.class);
    private final LicenseRegistryService service = new LicenseRegistryService(repo, historyRepo);

    private LicenseRegistry lic(String id, String pid, String hwid, String str) {
        LicenseRegistry r = new LicenseRegistry();
        r.setLicenseId(id);
        r.setProductId(pid);
        r.setHardwareId(hwid);
        r.setLicenseString(str);
        return r;
    }

    @Test
    @DisplayName("기존 없음 → NEW (uploadedBy 세팅, toSave=incoming)")
    void newWhenAbsent() {
        when(repo.findByLicenseIdAndProductId(any(), any())).thenReturn(Optional.empty());
        LicenseRegistry incoming = lic("L1", "P1", "HW", "STR");

        UpsertDecision d = service.classifyUpsert(incoming, "tester");

        assertThat(d.outcome()).isEqualTo(LicenseUpsertOutcome.NEW);
        assertThat(d.toSave()).isSameAs(incoming);
        assertThat(d.toSave().getUploadedBy()).isEqualTo("tester");
        assertThat(d.toSave().getUploadDate()).isNotNull();
    }

    @Test
    @DisplayName("기존 동일(HW·String 불변) → DUPLICATE (toSave=null)")
    void duplicateWhenUnchanged() {
        LicenseRegistry existing = lic("L1", "P1", "HW", "STR");
        when(repo.findByLicenseIdAndProductId("L1", "P1")).thenReturn(Optional.of(existing));

        UpsertDecision d = service.classifyUpsert(lic("L1", "P1", "HW", "STR"), "tester");

        assertThat(d.outcome()).isEqualTo(LicenseUpsertOutcome.DUPLICATE);
        assertThat(d.toSave()).isNull();
    }

    @Test
    @DisplayName("Hardware ID 변경 → UPDATED (기존 엔티티에 반영)")
    void updatedWhenHardwareChanged() {
        LicenseRegistry existing = lic("L1", "P1", "HW_OLD", "STR");
        when(repo.findByLicenseIdAndProductId("L1", "P1")).thenReturn(Optional.of(existing));

        UpsertDecision d = service.classifyUpsert(lic("L1", "P1", "HW_NEW", "STR"), "tester");

        assertThat(d.outcome()).isEqualTo(LicenseUpsertOutcome.UPDATED);
        assertThat(d.toSave()).isSameAs(existing);              // 기존 엔티티 갱신(merge)
        assertThat(d.toSave().getHardwareId()).isEqualTo("HW_NEW");
        assertThat(d.toSave().getUploadedBy()).isEqualTo("tester");
    }

    @Test
    @DisplayName("License String 변경 → UPDATED")
    void updatedWhenStringChanged() {
        LicenseRegistry existing = lic("L1", "P1", "HW", "STR_OLD");
        when(repo.findByLicenseIdAndProductId("L1", "P1")).thenReturn(Optional.of(existing));

        UpsertDecision d = service.classifyUpsert(lic("L1", "P1", "HW", "STR_NEW"), "tester");

        assertThat(d.outcome()).isEqualTo(LicenseUpsertOutcome.UPDATED);
        assertThat(d.toSave().getLicenseString()).isEqualTo("STR_NEW");
    }

    @Test
    @DisplayName("갱신 시 사용자 큐레이션 한글 필드 10개 전부 보존 (field-preservation)")
    void preservesUserCuratedFieldsOnUpdate() {
        // 기존: 사용자가 한글화한 보존 10필드
        LicenseRegistry existing = lic("L1", "P1", "HW", "STR_OLD");
        existing.setRegisteredTo("박욱진");
        existing.setFullName("박욱진");
        existing.setEmail("한글이메일@정도.kr");
        existing.setCompany("(주)정도유아이티");
        existing.setTelephone("전화-한글");
        existing.setFax("팩스-한글");
        existing.setStreet("서울특별시 강남구");
        existing.setCity("서울");
        existing.setZipCode("우06236");
        existing.setCountry("대한민국");
        when(repo.findByLicenseIdAndProductId("L1", "P1")).thenReturn(Optional.of(existing));

        // incoming(Derby): 보존 10필드 전부 로마자 + license_string 변경
        LicenseRegistry incoming = lic("L1", "P1", "HW", "STR_NEW");
        incoming.setRegisteredTo("Park Ukjin");
        incoming.setFullName("Park Ukjin");
        incoming.setEmail("eng@jungdo.kr");
        incoming.setCompany("JungdoUIT");
        incoming.setTelephone("02-000-0000");
        incoming.setFax("02-000-0001");
        incoming.setStreet("Gangnam-gu, Seoul");
        incoming.setCity("Seoul");
        incoming.setZipCode("06236");
        incoming.setCountry("Korea");
        incoming.setLicenseType("License Text");   // 기술 필드는 갱신되어야

        UpsertDecision d = service.classifyUpsert(incoming, "tester");

        assertThat(d.outcome()).isEqualTo(LicenseUpsertOutcome.UPDATED);
        // 기술 필드는 갱신
        assertThat(d.toSave().getLicenseString()).isEqualTo("STR_NEW");
        assertThat(d.toSave().getLicenseType()).isEqualTo("License Text");
        // 보존 10필드 전부 기존(한글)값 유지 — 어느 하나라도 setter 재추가 시 실패
        LicenseRegistry s = d.toSave();
        assertThat(s.getRegisteredTo()).isEqualTo("박욱진");
        assertThat(s.getFullName()).isEqualTo("박욱진");
        assertThat(s.getEmail()).isEqualTo("한글이메일@정도.kr");
        assertThat(s.getCompany()).isEqualTo("(주)정도유아이티");
        assertThat(s.getTelephone()).isEqualTo("전화-한글");
        assertThat(s.getFax()).isEqualTo("팩스-한글");
        assertThat(s.getStreet()).isEqualTo("서울특별시 강남구");
        assertThat(s.getCity()).isEqualTo("서울");
        assertThat(s.getZipCode()).isEqualTo("우06236");
        assertThat(s.getCountry()).isEqualTo("대한민국");
    }

    @Test
    @DisplayName("보존 필드 상수 = 10개 (목록 고정)")
    void preservedFieldsConstantHas10() {
        assertThat(LicenseRegistryService.PRESERVED_USER_FIELDS)
            .hasSize(10)
            .containsExactlyInAnyOrder("registeredTo", "fullName", "email", "company",
                "telephone", "fax", "street", "city", "zipCode", "country");
    }
}
