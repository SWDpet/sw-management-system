# 기획서 — 아키텍처 순환의존 해소 (security 패키지 분리)

> **상태**: 구현 완료 · 정식 워크플로 사후 정합(2026-06-23) · 작성 🧭기획팀
> **워크플로우**: 기획서(본 문서) → 🔍codex 검토 → ✅사용자 승인 → 개발계획 → 🔍codex 구현검증 → 커밋
> **차원**: 품질 로드맵 "아키텍처 B+→A" (QUALITY_CHARTER §0 게이트 기반 등급)

---

## 1. 배경 · 문제

아키텍처 불변식 게이트(`LayeredArchitectureTest`) 도입 중 **패키지 슬라이스 순환의존 1건**이 발견됨:

```
config → service → config  (cycle)
```

- **config → service**: 보안 핸들러(`AccessLogLogoutHandler`, `CustomAuthenticationSuccessHandler`, `CustomAuthenticationFailureHandler`)가 `LogService`·`LoginAttemptService` 호출.
- **service → config**: `LogService`/`UserDetailsServiceImpl` 가 `CustomUserDetails`, `LoginAttemptService` 가 `SecurityLoginProperties` 참조 — 이 두 클래스가 **`config` 패키지에 소재**.

순환의존은 진짜 아키텍처 스멜(레이어 경계 붕괴, 테스트·이해·변경 난이도↑)이며, 아키텍처 A(순환 0 게이트)의 마지막 잔여물.

## 2. 목표 · 비목표

- **목표**: config↔service 순환 제거 → `slices().beFreeOfCycles()` 하드게이트 추가 → 아키텍처 A.
- **비목표**: 컨트롤러→Repository 직접접근(별도 ratchet 동결, 의도적 read 패턴), 다른 레이어 규칙 신설.

## 3. 근본원인 · 해소 방향

`CustomUserDetails`(Spring Security `UserDetails` 구현, `User` 엔티티 래핑)와 `SecurityLoginProperties`(`@ConfigurationProperties` 로그인 잠금 설정)는 **service 가 의존하는 자원**인데 `config` 에 위치 → service→config 역방향 의존 발생.

**해소**: 두 클래스를 신규 **`com.swmanager.system.security`** 패키지로 이동. 그러면:
- service → security (정상, 하위 의존)
- config → service (정상, 단방향)
- security → domain 만 (CustomUserDetails→User)
→ 순환 소멸. security 패키지는 domain 외 어떤 상위 레이어에도 의존하지 않음.

## 4. ⚠ 리스크 · 배포 영향 (사용자 승인 필수 항목)

- **세션 직렬화**: `CustomUserDetails` 는 `Serializable` 이며 **HTTP 세션에 저장**됨. 패키지(=FQCN) 변경 → 기존 직렬화 세션 역직렬화 불가 → **다음 배포 시 접속 중 사용자 전원이 재로그인** 필요.
  - 영향 범위: 재로그인만(데이터 손실 없음). 기존에도 배포 시 세션 영향 사례 존재(project_pms_standalone_login 류 인증 변경 경험).
  - 완화: 배포 공지 또는 트래픽 적은 시간 배포. 롤백 시 역도 동일(재로그인).
- **참조 광범위**: `CustomUserDetails` 21파일 import + 템플릿 2종 `T()` instanceof. 기계적이나 누락 시 컴파일 실패로 즉시 포착(안전).

## 5. 비목표/대안 검토

- 대안 A(핸들러를 config 밖으로): config→service edge 만 이동, service→config(CustomUserDetails) 잔존 → 순환 미해소(기각).
- 대안 B(순환 ignore/freeze): 스멜 은폐일 뿐 실제 해소 아님 → A 자격 미흡(기각).
- **채택**: 자원 클래스 2종을 security 로 이동(근본 해소).

## 6. 검증 계획

- `LayeredArchitectureTest.no_package_cycles`(beFreeOfCycles) **통과** = 순환 0.
- 전체 단위 테스트 그린(패키지 이동 회귀 0).
- 잔존 `config.CustomUserDetails`/`config.SecurityLoginProperties` FQN 0건.
