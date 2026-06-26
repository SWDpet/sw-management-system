package com.swmanager.system.controller.ops;

import com.swmanager.system.domain.User;
import com.swmanager.system.domain.ops.Partner;
import com.swmanager.system.domain.ops.PartnerContact;
import com.swmanager.system.dto.ops.ContactForm;
import com.swmanager.system.dto.ops.PartnerForm;
import com.swmanager.system.repository.ops.PartnerContactRepository;
import com.swmanager.system.repository.ops.PartnerRepository;
import com.swmanager.system.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PartnerController(외부업체/협력사 관리) 단위 테스트 (beyond-A 커버리지 스프린트 — 컨트롤러 통합영역 11탄).
 *
 * <p>PartnerController 는 {@code @RequiredArgsConstructor}(2 의존성)이고 권한은
 * {@code @AuthenticationPrincipal CustomUserDetails} 메서드 파라미터(authPerson)에서 읽으므로,
 * mock 2종 생성자주입 + 사용자 파라미터 직접전달로 호출한다(SecurityContext 불필요).
 * 가드 위반은 403 ResponseEntity. 실 Postgres 불필요 → 기본 CI 에서 JaCoCo floor 반영.
 */
class PartnerControllerTest {

    private PartnerController controller;
    private PartnerRepository partnerRepository;
    private PartnerContactRepository partnerContactRepository;

    @BeforeEach
    void setUp() {
        partnerRepository = mock(PartnerRepository.class);
        partnerContactRepository = mock(PartnerContactRepository.class);
        controller = new PartnerController(partnerRepository, partnerContactRepository);
    }

    private static CustomUserDetails u(String authPerson) {
        User user = new User();
        user.setUserSeq(1L);
        user.setUserid("tester");
        user.setUsername("tester");
        user.setUserRole("ROLE_USER");
        user.setAuthPerson(authPerson);
        return new CustomUserDetails(user);
    }
    private static CustomUserDetails editU() { return u("EDIT"); }
    private static CustomUserDetails viewU() { return u("VIEW"); }
    private static CustomUserDetails noneU() { return u("NONE"); }

    private static PartnerForm partnerForm(String name) {
        return new PartnerForm(name, "협력사", "123-45-67890", "02-1234-5678", "비고");
    }
    private static ContactForm contactForm(String name) {
        return new ContactForm(name, "과장", "010-1111-2222", "a@b.c");
    }

    // ───────────────────────── 화면 ─────────────────────────

    @Test
    void manage_rendersWithCanEdit() {
        Model m = new ExtendedModelMap();
        assertThat(controller.manage(m, editU())).isEqualTo("ops-doc/partner-management");
        assertThat(m.getAttribute("canEdit")).isEqualTo(true);
    }

    // ───────────────────────── 목록 ─────────────────────────

    @Test
    void list_noView_empty() {
        assertThat(controller.list(noneU())).isEmpty();
        verify(partnerRepository, never()).findByUseYnOrderByNameAsc(any());
    }

    @Test
    void list_view_mapsPartnersAndContacts() {
        Partner p = new Partner();
        p.setPartnerId(1L);
        p.setName("협력업체A");
        when(partnerRepository.findByUseYnOrderByNameAsc("Y")).thenReturn(List.of(p));
        when(partnerContactRepository.findByPartner_PartnerIdAndUseYnOrderByNameAsc(1L, "Y"))
                .thenReturn(List.of());
        var rows = controller.list(viewU());
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).name()).isEqualTo("협력업체A");
    }

    // ───────────────────────── 업체 CRUD ─────────────────────────

    @Test
    void create_noEdit_forbidden() {
        assertThat(controller.create(partnerForm("X"), viewU()).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(partnerRepository, never()).save(any());
    }

    @Test
    void create_blankName_badRequest() {
        assertThat(controller.create(partnerForm("  "), editU()).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(partnerRepository, never()).save(any());
    }

    @Test
    void create_edit_savesPartner() {
        Partner saved = new Partner();
        saved.setPartnerId(9L);
        when(partnerRepository.save(any())).thenReturn(saved);
        ResponseEntity<?> res = controller.create(partnerForm("협력업체B"), editU());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        // 폼 → 엔티티 매핑 확인
        ArgumentCaptor<Partner> cap = ArgumentCaptor.forClass(Partner.class);
        verify(partnerRepository).save(cap.capture());
        assertThat(cap.getValue().getName()).isEqualTo("협력업체B");
        assertThat(cap.getValue().getBizNo()).isEqualTo("123-45-67890");
    }

    @Test
    void update_noEdit_forbidden() {
        assertThat(controller.update(1L, partnerForm("X"), viewU()).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void update_notFound_404() {
        when(partnerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThat(controller.update(1L, partnerForm("X"), editU()).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void update_edit_mapsFieldsAndSaves() {
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(new Partner()));
        assertThat(controller.update(1L, partnerForm("수정업체"), editU()).getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<Partner> cap = ArgumentCaptor.forClass(Partner.class);
        verify(partnerRepository).save(cap.capture());
        assertThat(cap.getValue().getName()).isEqualTo("수정업체");   // 폼 → 기존 엔티티 매핑
        assertThat(cap.getValue().getBizNo()).isEqualTo("123-45-67890");
    }

    @Test
    void delete_noEdit_forbidden() {
        assertThat(controller.delete(1L, viewU()).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(partnerRepository, never()).save(any());
    }

    @Test
    void delete_found_softDeletes() {
        Partner p = new Partner();
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(p));
        assertThat(controller.delete(1L, editU()).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(p.getUseYn()).isEqualTo("N"); // 소프트 삭제
        verify(partnerRepository).save(p);
    }

    @Test
    void delete_notFound_okNoSave() {
        // 의도된 멱등 계약: 미존재 삭제는 404 가 아니라 200(no-op). update 의 404 와 다른 contract.
        when(partnerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThat(controller.delete(1L, editU()).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(partnerRepository, never()).save(any());
    }

    // ───────────────────────── 담당자 CRUD ─────────────────────────

    @Test
    void addContact_noEdit_forbidden() {
        assertThat(controller.addContact(1L, contactForm("홍길동"), viewU()).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        verify(partnerContactRepository, never()).save(any());
    }

    @Test
    void addContact_partnerNotFound_404() {
        when(partnerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThat(controller.addContact(1L, contactForm("홍길동"), editU()).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        verify(partnerContactRepository, never()).save(any());
    }

    @Test
    void addContact_blankName_badRequest() {
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(new Partner()));
        assertThat(controller.addContact(1L, contactForm("  "), editU()).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        verify(partnerContactRepository, never()).save(any());
    }

    @Test
    void addContact_edit_savesContact() {
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(new Partner()));
        PartnerContact saved = new PartnerContact();
        saved.setContactId(21L);
        when(partnerContactRepository.save(any())).thenReturn(saved);
        ResponseEntity<?> res = controller.addContact(1L, contactForm("홍길동"), editU());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        ArgumentCaptor<PartnerContact> cap = ArgumentCaptor.forClass(PartnerContact.class);
        verify(partnerContactRepository).save(cap.capture());
        assertThat(cap.getValue().getName()).isEqualTo("홍길동");
    }

    @Test
    void deleteContact_noEdit_forbidden() {
        assertThat(controller.deleteContact(1L, viewU()).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(partnerContactRepository, never()).save(any());
    }

    @Test
    void deleteContact_found_softDeletes() {
        PartnerContact c = new PartnerContact();
        when(partnerContactRepository.findById(1L)).thenReturn(Optional.of(c));
        assertThat(controller.deleteContact(1L, editU()).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(c.getUseYn()).isEqualTo("N");
        verify(partnerContactRepository).save(c);
    }

    @Test
    void deleteContact_notFound_okNoSave() {
        when(partnerContactRepository.findById(1L)).thenReturn(Optional.empty());
        assertThat(controller.deleteContact(1L, editU()).getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(partnerContactRepository, never()).save(any());
    }
}
