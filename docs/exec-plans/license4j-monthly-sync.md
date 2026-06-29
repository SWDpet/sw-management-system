# 개발계획서 — License4J 라이선스 대장 매월 자동 연동 (license4j-monthly-sync)

> 상태: **v0.3 (구현 완료 — P0~P10, 실연동·멱등 검증 완료, 커밋 대기)** · 2026-06-29
> codex 1차: 조건부승인 → 보강 4건 반영. codex 2차: 승인. codex 구현검증: 수정필요 5건 → 실결함 4건 수정(센티넬·트랜잭션·동시성·복원), #2 CSV카운터는 의도된 회귀0.
> **실연동 검증(C)**: Product ID = **productName(+버전)** 정정(productIDString 아님, 1495 오삽입→스냅샷 복원으로 즉시 회수). 기존 대장 license_string 손상 1531건을 D2가 교정. **3차 실행 전건 중복=멱등 확인**. 단위 13/13 + 스모크 통과. 운영DB 대장=1582(교정+신규36), 이력/백업 정리.
> 기획서: `docs/product-specs/license4j-monthly-sync.md` (v0.4, 최종승인 완료 2026-06-29)
> 전제 확정: SMB 접근 **가능**(운영 WAR 호스트 → 192.168.10.188) → 앱 내 스케줄러 직접 실행 경로 채택.

---

## 0. 환경 사실(확인됨)
- `@EnableScheduling` 이미 활성(`SwManagerApplication`) → 스케줄러 추가만.
- DB 스키마 = 수동 SQL 스크립트(`src/main/resources/db_init_phase*.sql`) + `spring.jpa.hibernate.ddl-auto=none`. → 신규 테이블은 DDL 스크립트로 운영DB에 직접 적용([[feedback_swmanager_prod_db_direct_ok]]).
- pom.xml에 **Apache Derby 의존성 없음** → 추가 필요. License4J DB = Derby **10.10**.
- 기존 upsert 로직: `LicenseRegistryService.uploadCsvFile`(중복키 License ID+Product ID, 변경분 갱신). 컨트롤러 `LicenseRegistryController`.

---

## 1. 산출물 목록

### 신규
| 파일 | 용도 |
|------|------|
| `src/main/resources/db_license4j_sync.sql` | `license_sync_history`, `license_registry_backup` DDL |
| `license/domain/LicenseSyncHistory.java` | 연동 이력 엔티티(D1) |
| `license/domain/LicenseRegistryBackup.java` | 대장 스냅샷 엔티티 |
| `license/repository/LicenseSyncHistoryRepository.java` | |
| `license/repository/LicenseRegistryBackupRepository.java` | |
| `license/sync/DerbyLicenseReader.java` | SMB 복사 + Derby read-only 부팅 + APP.LICENSES⨝PRODUCTS 읽기 → raw row |
| `license/sync/LicenseFieldMapper.java` | raw row → `LicenseRegistry` 레코드 매핑 + 코드→라벨 + preflight 검증 |
| `license/sync/LicenseCodeLabelMap.java` | 코드↔라벨 실증 매핑 상수(§4) |
| `license/sync/LicenseSyncService.java` | 오케스트레이션(복사→백업→매핑→upsert→이력) |
| `license/sync/LicenseSyncScheduler.java` | `@Scheduled` 월말 트리거 |
| `license/controller/LicenseSyncController.java` | ADMIN 수동 트리거 엔드포인트 |

### 수정
| 파일 | 변경 |
|------|------|
| `pom.xml` | Derby 의존성 추가 |
| `license/service/LicenseRegistryService.java` | per-record **`upsertOne(LicenseRegistry, source, counters)` 추출**(기존 `uploadCsvFile`이 이를 호출하도록 — 동작 동일) |
| `templates/license/registry-upload.html` 또는 이력 화면 | 연동 이력 탭/구분 추가(FR-7), guide-box ③ stale 문구 정정(C-3) |
| `application.properties` / `.env.example` | L4J 연동 설정키 추가 |

---

## 2. 작업 순서 (Phase)

### P0. Derby 호환 선행 게이트 (T-D) — ✅ 통과 (2026-06-29)
> **검증 완료**: Maven `org.apache.derby:derby:10.14.2.0`로 License4J Derby 10.10 DB를 라이브 사본에서 `readOnly=true` 부팅 성공(licenses 1567 읽음, 260ms) + 일반 부팅도 OK. 드라이버 버전 조정 불필요. 운영 경로(복사→readOnly 부팅→SELECT) 그대로 채택.

- 본 기능 구현 착수 **전에** `org.apache.derby:derby:10.14.2.0`가 License4J Derby **10.10** DB를 **사본 + read-only 부팅 + 부팅 성공 후 폐기**로 정상 read하는지 1회 검증.
- 통과 → P3 진행. 실패 → 드라이버 버전 조정(10.15/10.16 split: derby+derbyshared+derbytools) 후 재검증. 이 게이트 통과 전 P3 이후 작업 보류.
- (참고: 본 기획 단계에서 Probe로 libs.jar 번들 Derby로는 read 검증 완료 — Maven 의존 드라이버 버전 확정만 남음.)

### P1. DB DDL (FR-4, FR-7, D1)
- `license_sync_history`: id, run_type(AUTO/MANUAL), started_at, finished_at, status(SUCCESS/FAIL/PARTIAL), total_count, new_count, updated_count, duplicate_count, fail_count, source_snapshot_path, message, triggered_by.
- `license_registry_backup`: id, snapshot_id(UUID/run FK), snapshot_at, + `license_registry` 전 컬럼 미러(또는 jsonb payload). 보관정책 N개월(파라미터화).
- 운영DB 직접 적용([[reference_swmanager_db_test_run]] 좌표). **롤백 SQL 동봉**(DROP).

### P2. upsert 코어 추출 (NFR-3, D2, FR-7) — 회귀 0이 최우선
- `uploadCsvFile`의 행별 처리(중복 탐색 → 변경분 판정 → 신규/갱신/스킵 분류)를 `upsertOne(LicenseRegistry rec, String source, String uploadedBy)`로 추출.
- **결과 타입 명시(codex 보강 #1)**: `enum LicenseUpsertOutcome { NEW, UPDATED, DUPLICATE, FAILED }` 반환. 호출측이 **`newCount`/`updatedCount`/`duplicateCount`/`failCount`를 분리 집계**. → 현 코드가 신규+갱신을 `successCount`에 뭉개고 `newCount=successCount`로 반환하는 문제를 연동 경로에서 해소(FR-7 "총/신규/갱신/중복/실패"). 단 **기존 `uploadCsvFile` 반환 Map 형태·기존 화면은 불변**(연동 경로만 분리 카운터 사용; 수동 업로드 회귀 0).
- `uploadCsvFile`은 파싱만 담당하고 행마다 `upsertOne` 호출. **수동 업로드 동작·파서·컨트롤러·`LicenseUploadHistory` 의미 100% 보존**.
- 검증: 기존 CSV 업로드 회귀 테스트(동일 입력 → 동일 신규/중복/실패 카운트).

### P3. Derby Reader (FR-1, FR-5, NFR-1) — SMB 접근 가능 전제
- pom.xml: `org.apache.derby:derby:10.14.2.0`(단일 jar, Java17 구동·10.10 DB read 호환. ⚠T-D로 호환 검증, 실패 시 10.15/10.16 split jar 대안).
- 절차: SMB로 `license4jdb` 폴더를 앱서버 임시 경로 복사(= 그달 백업본) → 임시 사본 `db.lck` 제거 → `jdbc:derby:<copy>` read-only 부팅(**부팅 성공=일관성 게이트**, 실패 시 재복사 1회 후 스킵+경보) → 쿼리 → 종료 → 임시 사본 정리(백업본은 월별 보관).
- SQL: 컬럼 **quoted mixed-case** 식별자 필수(미인용 시 Derby가 대문자화 → 42X04). `licenseKeyPair`는 **SELECT 안 함**(개인키 미접근, NFR-2). 전체 예시(codex 보강 #4):
  ```sql
  SELECT l."licenseID", p."productIDString", l."licenseString", l."hardwareID",
         l."userRegisteredTo", l."userFullName", l."userEMail", l."userCompany",
         l."userTelephone", l."userFax", l."userStreet", l."userCity",
         l."userZipCode", l."userCountry", l."quantity", l."validProductEdition",
         l."validProductVersion", l."validityPeriod", l."maintenancePeriod",
         l."generationDateTime", l."generationSource", l."selectedLicenseType",
         l."selectedActivationReturn", l."selectedHardwareIDSelection",
         l."selectedDateTimeCheck" /* ...63 표준필드 대응 컬럼 전체 */
  FROM APP.LICENSES l JOIN APP.PRODUCTS p ON l."productID" = p."productID"
  ```

### P4. 코드→라벨 실증 매핑 (FR-3, T-2)
- 운영 `license_registry`(라벨) ↔ Derby `APP.LICENSES`(코드)를 같은 licenseID로 조인 → 코드↔라벨 대응표 추출(1회 분석, `LicenseCodeLabelMap` 상수화).
- 대상: `selectedLicenseType`, `generationSource`, `selectedActivationReturn`, `selectedHardwareIDSelection`, `selectedDateTimeCheck` + SMALLINT(0/1)→`True/False`.
- **미매핑 코드값 = 해당 건 실패 처리 + 경보 로그**(원시 코드 무단 저장 금지).

### P5. 매퍼 + preflight (FR-2, C-1, T-3)
- `LicenseFieldMapper`: Derby row → `LicenseRegistry`. 직통(License ID←licenseID, Product ID←productIDString, Generation Date Time←generationDateTime 문자열, Hardware ID←hardwareID `&&`보존, License String←licenseString **다중라인 그대로**) + 코드→라벨(P4).
- **표준필드 매핑 완전성 검증(preflight)**: 63 표준필드 매핑 누락/필수값 결손/타입·날짜·불리언 포맷/License String 비어있음 → 건별 실패 + 사유. (CSV 헤더 검증 아님)

### P6. 동기화 오케스트레이션 (FR-2,4,6,7, NFR-5)
- `LicenseSyncService.run(runType, triggeredBy)`:
  1. `license_sync_history` 시작행(RUNNING).
  2. **pre-sync 스냅샷** → `license_registry_backup`.
  3. P3 Derby 읽기 → P5 매핑/preflight.
  4. **청크 단위**로 `upsertOne` 호출(P2 공유 코어), 신규/갱신/중복/실패 분리 집계.
  5. 마감 + 이력 상태.
- **롤백 기준 명확화(codex 보강 #2)**:
  - **건별 실패**(preflight 결손·미매핑 코드·단건 예외) → 해당 건만 스킵+사유 기록, **성공분은 유지**, 런 상태 = **PARTIAL**. (월간 연동은 1건 불량으로 전체 중단 안 함)
  - **치명적 실패**(Derby 읽기 불가·DB 연결 끊김·청크 커밋 중 중대 오류) → 런 **ABORT**, **직전 pre-sync 스냅샷(`license_registry_backup`)으로 전량 복원**, 상태 = **FAIL** + 경보.
  - T-5는 두 경로(PARTIAL 유지 / FAIL 전량복원) 모두 검증.
- 동시 실행 방지(락/플래그) — 자동·수동 트리거 중복 실행 차단.

### P7. 스케줄러 + 수동 트리거 (FR-1, FR-6, T-6)
- `LicenseSyncScheduler`: `@Scheduled(cron=...)` 월말(설정키). 실행 주체=시스템.
- `LicenseSyncController`: `POST /license/sync/run` — **ADMIN 가드**(기존 권한 패턴, [[feedback_all_pages_require_permission]]). 결과 리다이렉트+메시지.

### P8. 이력 화면 (FR-7)
- 기존 업로드 이력 화면에 **연동 이력 탭/구분** 추가(신규 페이지 X). 컬럼: 일시·구분(자동/수동)·총/신규/갱신/중복/실패·상태. (UI 키워드 → §디자인 자문 체크: 탭/표만 추가, 토큰 신규 X → 경미하나 [[feedback_ui_change_always_design_consult]] 가상자문 1회 거침)

### P9. 보안 (NFR-2)
- SMB 자격증명·경로 = 환경변수/시크릿(코드·git 하드코딩 금지, `docs/AGENT_SAFETY.md`).
- 로그·예외에 SMB 계정/경로, `licenseKeyPair`, License String 원문 미기록(마스킹).

### P10. 테스트 (T-1~T-6 + 보강)
- T-D(신규): Derby 10.14 드라이버가 10.10 DB read-only 부팅 성공.
- T-1 행수, T-2 매핑표 일치(+미매핑 실패 케이스), T-3 preflight 실패 케이스 + idempotent(재실행 전건 중복 스킵), T-4 HWID/License String 변경 1건→갱신 1건, T-5 스냅샷 백업·**실제 롤백**, T-6 스케줄러=시스템·수동=ADMIN 가드.
- 회귀: 기존 CSV 업로드(P2 리팩터 후) 동일 동작.
- DB 포함 테스트 실행법: [[reference_swmanager_db_test_run]](RUN_DB_TESTS=true + 좌표 5종).

---

## 3. 설정/환경변수
- `license4j.sync.derby-unc` = `\\192.168.10.188\C$\Users\Administrator\AppData\Local\License4J License Manager\license4jdb`
- `license4j.sync.smb-user` / `smb-pass` (시크릿 채널)
- `license4j.sync.cron` = 월말 (예 `0 0 2 L * ?` 류 — 구현 시 확정)
- `license4j.sync.backup-dir` = Derby 폴더 월별 백업 보관 경로
- `license4j.sync.backup-retention-months`

## 4. 롤백 전략
- DDL: DROP 스크립트 동봉.
- 코드: P2 리팩터는 동작 동등(회귀 테스트 게이트). 연동 기능은 신규 격리 → 비활성(cron off/엔드포인트 가드)으로 무력화 가능.
- 데이터: 매 실행 pre-sync 스냅샷 → `license_registry_backup`에서 복원.

## 5. 구현 순서 의존성
**P0(선행 게이트)** → P1 → P2(독립) → P3 → P4 → P5 → P6 → (P7, P8 병렬) → P9 → P10. P2는 P1과 무관하게 선행 가능(회귀 먼저 확보). P3 이후 작업은 P0 통과가 전제.

## 6. FR/NFR/T 추적표
- FR-1 P3·P7 / FR-2 P2·P5 / FR-3 P4 / FR-4 P1·P6 / FR-5 P3 / FR-6 P7 / FR-7 P1·P6·P8
- NFR-1 P3 / NFR-2 P3·P9 / NFR-3 P2 / NFR-4 P4(enum 미도입·라벨 보존) / NFR-5 P6
- T-D P0(선행 게이트)·P10 / T-1~T-6 P10

## 7. 미결/검증 게이트
- T-D(Derby 10.14↔10.10 호환) 실패 시 드라이버 버전 조정.
- P4 매핑표에서 기존 대장에 없는 코드값 발견 시 정책=차단(이미 확정), 빈도 점검.
- 월말 cron 정확 표현식·타임존(Asia/Seoul) 구현 시 확정.
