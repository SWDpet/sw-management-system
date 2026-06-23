# 개발계획서 — 아키텍처 순환의존 해소 (security 패키지 분리)

- **기획서**: `docs/product-specs/arch-decycle-security-package.md`
- **상태**: 구현 완료 · codex 구현검증 대기 → 사용자 승인 → 커밋
- **작성일**: 2026-06-23

---

## 1. 변경 묶음

### A. 패키지 이동 (git mv — 이력 보존)
- `config/CustomUserDetails.java` → `security/CustomUserDetails.java`
- `config/SecurityLoginProperties.java` → `security/SecurityLoginProperties.java`
- 두 파일 `package com.swmanager.system.config;` → `package com.swmanager.system.security;`

### B. 참조 갱신 (FQN/import, 29파일)
- 컨트롤러·서비스 21 + 테스트 5 + 템플릿 2(`person-list.html`/`project-list.html` 의 `T(com.swmanager.system.config.CustomUserDetails)` → `...security...`).
- 안전한 일괄 치환(Python read→replace→write, exact-string, sed 미사용 — feedback_no_sed_inplace 준수). 치환 후 잔존 FQN 0건 grep 검증.

### C. 동일패키지 사용처 import 추가 (5파일)
- 이동 전 `config` 내부에서 import 없이 쓰던 곳: `AccessLogLogoutHandler`, `CustomAuthenticationSuccessHandler`(main) + `AccessLogLogoutHandlerTest`, `CustomAuthenticationSuccessHandlerTest`, `SecurityLoginPropertiesValidationTest`(test) → `import com.swmanager.system.security.*` 추가.

### D. 게이트 추가
- `LayeredArchitectureTest` 에 `no_package_cycles`(`slices().matching(...).should().beFreeOfCycles()`) 하드게이트 추가. 클래스 Javadoc 의 "순환 잔여" 문구 → "해소" 갱신.

## 2. 변경 파일 (37)
- 이동 2(RM) + 참조갱신 29 + import추가 5(중 일부 참조갱신과 중복 카운트) + 테스트 1(LayeredArchitectureTest). `git status` 기준 37 entries(M 35 + RM 2).

## 3. 검증 결과
- ✅ `mvnw compile` 그린(미해결 심볼 0 — 핸들러/테스트 import 보강 후).
- ✅ `LayeredArchitectureTest` 5규칙 통과 = **순환 0** + 도메인순수성·상향의존금지·Repo인터페이스·명명.
- ✅ `ControllerRepositoryRatchetTest` 통과(baseline 295 불변).
- ✅ 전체 **526 테스트** 그린(52 DB-skip).
- ✅ 잔존 `config.CustomUserDetails`/`config.SecurityLoginProperties` FQN **0건**.

## 4. 배포 런북 (운영 반영 시)
- 배포 시 **세션 무효화 → 사용자 재로그인** 안내(§기획서 4). 트래픽 적은 시간 권장.
- 추가 설정 변경 없음(스키마·env 불변). 롤백 = 코드 revert(역시 재로그인 1회).

## 5. 롤백
- 코드 revert 로 즉시 복귀(패키지 원위치). 데이터/스키마 영향 없음.

## 6. codex 검토 체크
- [ ] 순환 실제 해소(beFreeOfCycles 통과) · [ ] 참조 누락 0(컴파일/grep) · [ ] 회귀 0(526 그린) · [ ] 세션영향 명시 · [ ] 롤백 타당
