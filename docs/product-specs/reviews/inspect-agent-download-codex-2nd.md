# codex 2차 검토 — inspect-agent-download 기획서 v0.3

- 검토일: 2026-06-02 (회사 PC) · codex v0.136.0 (gpt-5.5, read-only)
- 대상: `docs/product-specs/inspect-agent-download.md` v0.3 (codex 1차 + 디자인 자문 반영분)

**최종판정 = ⚠수정필요** (방향상 승인 가능에 근접, 수정 포인트는 작음)

## 1차 지적 반영 여부
| 항목 | 상태 | 비고 |
|---|---|---|
| FR-1 Maven phase/WAR포함/stale | **부분해소** | assembly·target/classes/agent·clean stale 명시됨. 단 WAR 생성도 `package` phase라 zip이 `war:war` 전에 들어간다는 보장 약함 → **`prepare-package`** 권장 |
| FR-3 Content-Type/Length/404 | **해소** | application/zip·Content-Length·404·403 명시 |
| FR-5 버전 소스 | **부분해소** | 현 `agent-windows/manifest.json` version=1 은 **런타임 실행순서 manifest**(배포버전 아님). `VERSION` 파일도 현재 없음 → 역할 분리 필요 |
| FR-6 무결성 | **부분해소** | zip SHA-256·파일목록 해시 OK. 단 기존 manifest.json 에 빌드정보 넣으면 런타임/배포 용도 혼선 → 별도 파일 권장 |
| NFR-1 업무권한 | **해소** | `authDocument != NONE`(EDIT 권장) |
| NFR-5 재현성 | **부분해소** | "동일 zip 바이트" 기준은 OK. plugin 버전 고정·`project.build.outputTimestamp`·entry order 등 구체 설정 부족 |
| T-6 | **해소** | 동일 zip 바이트로 명확 |
| §7 5건 | **대체로 해소** | 경로·권한·zip범위·assembly OK. 버전소스·zip범위는 실제 파일 기준 추가 정리 |

## 디자인 자문(NFR-3) 반영 — **해소**
- `--muted` 금지→`--text2`, 토큰전용·`:root` 재정의 금지, 다크 자동대응(전용 dark 블록 미추가), 클래스 스코프·`!important` 미사용 모두 반영.

## 남은 리스크 / 신규 발견 (codex 실파일 확인)
1. **Maven phase** — `package` phase 에 assembly→`target/classes` 주입은 WAR 생성과 순서 경합 위험. **`prepare-package` 이전 배치 후 WAR 생성**으로 선후관계 명시 필요.
2. **재현성** — "reproducible 옵션" 한 줄 부족. plugin 버전 고정, `project.build.outputTimestamp`/assembly outputTimestamp, 파일 정렬 보장 검증을 개발계획에 요구. Windows timestamp·줄끝·권한비트·zip entry order 흔들림 주의.
3. **zip 범위 실파일** — `config/site.gj.json`, `site.dyg.json` 실제 존재 / `site.example.json` 없음. "샘플만 포함, 실 config 제외" 방향 맞으나 **구현 전 샘플 생성 또는 include/exclude 규칙 확정** 필수.
4. **manifest 역할 분리** — 기존 `agent-windows/manifest.json` = 수집모듈 **실행순서** manifest. 빌드시각·SHA-256 넣으면 런타임/배포 혼선 → zip 내부에 **별도 `release-manifest.json` 생성** 권장.

## 권고 (→ 최종승인 조건)
- FR-1 Maven phase 를 **`prepare-package` 중심**으로 정정
- FR-5/FR-6 **배포 버전 파일(VERSION) + 배포 manifest(release-manifest.json)** 를 런타임 manifest 와 **역할 분리**
- NFR-5 에 **reproducible 설정 검증 기준 구체화**(plugin 버전·outputTimestamp·정렬)
→ 위 반영 시 **최종승인 가능**.
