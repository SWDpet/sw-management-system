package com.swmanager.system.lsa.service;

import com.swmanager.system.domain.PersonInfo;
import com.swmanager.system.lsa.domain.Lsa;
import com.swmanager.system.lsa.dto.LsaDTO;
import com.swmanager.system.lsa.dto.LsaForm;
import com.swmanager.system.lsa.repository.LsaRepository;
import com.swmanager.system.repository.PersonInfoRepository;
import com.swmanager.system.repository.SigunguCodeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** LsaService 단위테스트 (검색·DTO 매핑 + create ps_info upsert dedup, 순수 mock). */
class LsaServiceTest {

    private final LsaRepository repo = mock(LsaRepository.class);
    private final PersonInfoRepository personRepo = mock(PersonInfoRepository.class);
    private final SigunguCodeRepository sigunguRepo = mock(SigunguCodeRepository.class);
    private final com.swmanager.system.repository.UserRepository userRepo = mock(com.swmanager.system.repository.UserRepository.class);
    private final LsaService service = new LsaService(repo, personRepo, sigunguRepo, userRepo);

    private Lsa sample() {
        Lsa l = new Lsa();
        l.setId(7L);
        l.setCityNm("전라남도"); l.setDistNm("강진군");
        l.setDeptNm("정보화"); l.setTeamNm("GIS");
        l.setUserNm("홍길동"); l.setTel("061-123-4567"); l.setEmail("hong@x.go.kr");
        l.setVersion("v2.1"); l.setIssuer("박욱진");
        l.setCreatedAt(LocalDateTime.of(2026, 6, 29, 10, 0));
        return l;
    }

    private LsaForm form() {
        LsaForm f = new LsaForm();
        f.setCityNm("전라남도"); f.setDistNm("강진군"); f.setDeptNm("정보화"); f.setTeamNm("GIS");
        f.setUserNm("홍길동"); f.setTel("061-123-4567"); f.setEmail("hong@x.go.kr"); f.setVersion("v2.1");
        return f;
    }

    private PersonInfo person(Long id, String userNm, String tel, String email) {
        PersonInfo p = new PersonInfo();
        p.setId(id); p.setUserNm(userNm); p.setTel(tel); p.setEmail(email);
        return p;
    }

    // ===== 목록/검색 =====
    @Test
    void list_nullKeyword_passesNullAndMapsAllFields() {
        when(repo.search(isNull())).thenReturn(List.of(sample()));
        List<LsaDTO> result = service.list(null);
        verify(repo).search(isNull());
        LsaDTO d = result.get(0);
        assertThat(d.id()).isEqualTo(7L);
        assertThat(d.cityNm()).isEqualTo("전라남도");
        assertThat(d.userNm()).isEqualTo("홍길동");
        assertThat(d.version()).isEqualTo("v2.1");
        assertThat(d.issuer()).isEqualTo("박욱진");
    }

    @Test
    void list_blankKeyword_normalizedToNull() {
        when(repo.search(isNull())).thenReturn(List.of());
        service.list("   ");
        verify(repo).search(isNull());
    }

    @Test
    void list_valueKeyword_trimmedAndPassed() {
        when(repo.search("강진")).thenReturn(List.of());
        service.list("  강진  ");
        verify(repo).search("강진");
    }

    // ===== create — ps_info upsert dedup =====

    /** 1. 기존 담당자(이름+전화 일치) 매칭 → ps_info INSERT 0, 기존 id 재사용. */
    @Test
    void create_existingPerson_reusesIdNoInsert() {
        when(personRepo.findCandidates("전라남도", "강진군", "정보화", "GIS"))
                .thenReturn(List.of(person(55L, "홍길동", "061-123-4567", "other@x.kr")));
        when(repo.save(any(Lsa.class))).thenAnswer(i -> i.getArgument(0));

        service.create(form(), "박욱진", "ukjin");

        verify(personRepo, never()).save(any());   // 기존 재사용 → INSERT 없음
        ArgumentCaptor<Lsa> cap = ArgumentCaptor.forClass(Lsa.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().getPsInfoId()).isEqualTo(55L);
    }

    /** 2. 새 담당자(후보 없음) → ps_info INSERT, 신규 id 연결. */
    @Test
    void create_newPerson_insertsAndLinksId() {
        when(personRepo.findCandidates(any(), any(), any(), any())).thenReturn(List.of());
        when(personRepo.save(any(PersonInfo.class))).thenAnswer(i -> { PersonInfo p = i.getArgument(0); p.setId(99L); return p; });
        when(repo.save(any(Lsa.class))).thenAnswer(i -> i.getArgument(0));

        service.create(form(), "박욱진", "ukjin");

        verify(personRepo).save(any(PersonInfo.class));
        ArgumentCaptor<Lsa> cap = ArgumentCaptor.forClass(Lsa.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().getPsInfoId()).isEqualTo(99L);
    }

    /** 3. tel·email 둘 다 빈값 → 중복판정 금지, 항상 새 INSERT. */
    @Test
    void create_noTelNoEmail_alwaysInserts() {
        LsaForm f = form(); f.setTel(" "); f.setEmail(null);
        // 후보가 있어도(이름 동일) tel·email 둘 다 빈값이면 매칭 시도조차 안 함
        when(personRepo.save(any(PersonInfo.class))).thenAnswer(i -> { PersonInfo p = i.getArgument(0); p.setId(77L); return p; });
        when(repo.save(any(Lsa.class))).thenAnswer(i -> i.getArgument(0));

        service.create(f, "박욱진", "ukjin");

        verify(personRepo).save(any(PersonInfo.class));   // 무조건 INSERT
        verify(personRepo, never()).findCandidates(any(), any(), any(), any());
    }

    /** 4. 정규화(전화 하이픈/이메일 대소문자) 매칭 → 기존 재사용. */
    @Test
    void create_normalizedMatch_reusesId() {
        LsaForm f = form(); f.setTel("0611234567"); f.setEmail("HONG@X.GO.KR");   // 하이픈 없음·대문자
        when(personRepo.findCandidates(any(), any(), any(), any()))
                .thenReturn(List.of(person(60L, " 홍길동 ", "061-123-4567", "hong@x.go.kr")));
        when(repo.save(any(Lsa.class))).thenAnswer(i -> i.getArgument(0));

        service.create(f, "박욱진", "ukjin");

        verify(personRepo, never()).save(any());
        ArgumentCaptor<Lsa> cap = ArgumentCaptor.forClass(Lsa.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().getPsInfoId()).isEqualTo(60L);
    }

    /** 5. Lsa 필드 매핑(발급자·작성자·전 필드). */
    @Test
    void create_mapsAllLsaFields() {
        when(personRepo.findCandidates(any(), any(), any(), any())).thenReturn(List.of());
        when(personRepo.save(any(PersonInfo.class))).thenAnswer(i -> { PersonInfo p = i.getArgument(0); p.setId(1L); return p; });
        when(repo.save(any(Lsa.class))).thenAnswer(i -> i.getArgument(0));

        service.create(form(), "관리자발급", "ukjin");

        ArgumentCaptor<Lsa> cap = ArgumentCaptor.forClass(Lsa.class);
        verify(repo).save(cap.capture());
        Lsa l = cap.getValue();
        assertThat(l.getCityNm()).isEqualTo("전라남도");
        assertThat(l.getDistNm()).isEqualTo("강진군");
        assertThat(l.getDeptNm()).isEqualTo("정보화");
        assertThat(l.getTeamNm()).isEqualTo("GIS");
        assertThat(l.getUserNm()).isEqualTo("홍길동");
        assertThat(l.getVersion()).isEqualTo("v2.1");
        assertThat(l.getIssuer()).isEqualTo("관리자발급");
        assertThat(l.getCreatedBy()).isEqualTo("ukjin");
    }

    /** 6. 서버측 필수값 검증 — 버전 누락 시 거부(INSERT 없음). */
    @Test
    void create_missingRequired_throwsNoInsert() {
        LsaForm f = form(); f.setVersion("  ");   // 버전 누락
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.create(f, "박욱진", "ukjin"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repo, never()).save(any());
        verify(personRepo, never()).save(any());
    }

    // ===== getById / update / delete (P4) =====

    @Test
    void getById_found_mapsDto() {
        when(repo.findById(7L)).thenReturn(java.util.Optional.of(sample()));
        assertThat(service.getById(7L).userNm()).isEqualTo("홍길동");
    }

    @Test
    void getById_notFound_throws() {
        when(repo.findById(404L)).thenReturn(java.util.Optional.empty());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.getById(404L))
                .isInstanceOf(com.swmanager.system.exception.ResourceNotFoundException.class);
    }

    @Test
    void update_existing_updatesFieldsAndUpdatedBy() {
        Lsa existing = sample();
        when(repo.findById(7L)).thenReturn(java.util.Optional.of(existing));
        when(personRepo.findCandidates(any(), any(), any(), any())).thenReturn(List.of());
        when(personRepo.save(any(PersonInfo.class))).thenAnswer(i -> { PersonInfo p = i.getArgument(0); p.setId(88L); return p; });
        when(repo.save(any(Lsa.class))).thenAnswer(i -> i.getArgument(0));

        LsaForm f = form(); f.setVersion("v9.9");
        service.update(7L, f, "수정발급자", "editor");

        ArgumentCaptor<Lsa> cap = ArgumentCaptor.forClass(Lsa.class);
        verify(repo).save(cap.capture());
        Lsa saved = cap.getValue();
        assertThat(saved.getVersion()).isEqualTo("v9.9");
        assertThat(saved.getIssuer()).isEqualTo("수정발급자");
        assertThat(saved.getUpdatedBy()).isEqualTo("editor");
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getPsInfoId()).isEqualTo(88L);
    }

    /** 발급자 보존: issuerOverride=null → 기존 발급자 유지(편집자 이름 덮어쓰기 금지). */
    @Test
    void update_nullIssuerOverride_preservesExistingIssuer() {
        Lsa existing = sample();   // issuer="박욱진"
        when(repo.findById(7L)).thenReturn(java.util.Optional.of(existing));
        when(personRepo.findCandidates(any(), any(), any(), any())).thenReturn(List.of());
        when(personRepo.save(any(PersonInfo.class))).thenAnswer(i -> { PersonInfo p = i.getArgument(0); p.setId(1L); return p; });
        when(repo.save(any(Lsa.class))).thenAnswer(i -> i.getArgument(0));

        service.update(7L, form(), null, "editor");   // override=null

        ArgumentCaptor<Lsa> cap = ArgumentCaptor.forClass(Lsa.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().getIssuer()).isEqualTo("박욱진");   // 기존 발급자 보존
        assertThat(cap.getValue().getUpdatedBy()).isEqualTo("editor");
    }

    @Test
    void update_notFound_throws() {
        when(repo.findById(404L)).thenReturn(java.util.Optional.empty());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.update(404L, form(), "i", "u"))
                .isInstanceOf(com.swmanager.system.exception.ResourceNotFoundException.class);
        verify(repo, never()).save(any());
    }

    @Test
    void delete_existing_deletesLsaOnly() {
        Lsa existing = sample();
        when(repo.findById(7L)).thenReturn(java.util.Optional.of(existing));
        service.delete(7L);
        verify(repo).delete(existing);
        verify(personRepo, never()).delete(any());   // ps_info 보존
    }

    @Test
    void delete_notFound_throws() {
        when(repo.findById(404L)).thenReturn(java.util.Optional.empty());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.delete(404L))
                .isInstanceOf(com.swmanager.system.exception.ResourceNotFoundException.class);
    }

    // ===== P3-b: 발급자 후보 + 부서/팀 prefill =====

    /** issuerCandidates: 활성 사용자 실명 distinct·정렬·blank 제거. */
    @Test
    void issuerCandidates_distinctSortedNonBlank() {
        com.swmanager.system.domain.User a = new com.swmanager.system.domain.User(); a.setUsername("박욱진");
        com.swmanager.system.domain.User b = new com.swmanager.system.domain.User(); b.setUsername("김한준");
        com.swmanager.system.domain.User c = new com.swmanager.system.domain.User(); c.setUsername("박욱진"); // 중복
        com.swmanager.system.domain.User d = new com.swmanager.system.domain.User(); d.setUsername("  ");   // blank
        when(userRepo.findByEnabledTrue()).thenReturn(List.of(a, b, c, d));

        assertThat(service.issuerCandidates()).containsExactly("김한준", "박욱진");   // 정렬·중복/blank 제거
    }

    /** findPersonCandidates: PersonRow 에 부서/팀도 매핑(prefill 용). */
    @Test
    void findPersonCandidates_mapsDeptTeam() {
        PersonInfo p = new PersonInfo();
        p.setCityNm("전라남도"); p.setDistNm("강진군"); p.setDeptNm("도시과"); p.setTeamNm("GIS팀");
        p.setUserNm("홍길동"); p.setTel("061-1"); p.setEmail("h@x.kr");
        when(personRepo.findCandidates("전라남도", "강진군", null, null)).thenReturn(List.of(p));

        var rows = service.findPersonCandidates("전라남도", "강진군", null, null);
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).deptNm()).isEqualTo("도시과");
        assertThat(rows.get(0).teamNm()).isEqualTo("GIS팀");
        assertThat(rows.get(0).userNm()).isEqualTo("홍길동");
    }

    // ===== 캐스케이드 =====
    @Test
    void districtList_mapsSggNmDistinct() {
        when(sigunguRepo.findBySidoNmOrderBySggNm("전라남도")).thenReturn(List.of());
        assertThat(service.districtList("전라남도")).isEmpty();
        assertThat(service.districtList("  ")).isEmpty();   // blank → 빈
    }
}
