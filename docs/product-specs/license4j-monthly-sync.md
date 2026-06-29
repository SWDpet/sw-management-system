# 기획서 — License4J 라이선스 대장 매월 자동 연동 (license4j-monthly-sync)

> 상태: **v0.4 (codex 1·2차 검토 반영 + 문서 정합성 정정 완료, 최종승인 대기)** · 작성: 기획팀(Claude) · 2026-06-29
> 워크플로: 본 기획서 → codex 검토 → 사용자 최종승인 → 개발계획(`docs/exec-plans/`) → codex → 승인 → 구현

---

## 1. 배경 / 목적

라이선스 대장(`license_registry`)은 현재 **License4J License Manager(라이선스 서버 192.168.10.188)** 에서 담당자가 **id별로 CSV를 수동 export → SW Manager에 수동 업로드**하는 방식으로 갱신된다. 매월 반복되는 수작업이며 누락·지연 위험이 있다.

**목적**: 라이선스 서버의 License4J 데이터를 **매월 말 자동으로 SW Manager 대장에 반영**한다. 단, **기존 CSV 업로드 기준(중복·갱신·파싱 규칙)을 100% 그대로 준수**하고, 반영 전 기존 대장을 백업한다.

---

## 2. 현황 분석 (2026-06-29 회사 내부망에서 실측 검증 완료)

| 항목 | 사실 |
|------|------|
| 데이터 위치 | `\\192.168.10.188\C$\Users\Administrator\AppData\Local\License4J License Manager\license4jdb` |
| DB 종류 | **Apache Derby 10.10 임베디드 DB, 암호화 안 됨**(boot password 불필요) — JDBC로 직접 읽힘 |
| 라이선스 테이블 | `APP.LICENSES` **1,567행**. `licenseString`(라이선스 실체) + 등록자/회사/하드웨어ID/제품/유효기간/생성일시 등 기존 CSV 컬럼 거의 1:1 보유 |
| 제품 테이블 | `APP.PRODUCTS` 15행. `productIDString`(예 `ugis-sdk-professional`), `productName`, ⚠`licenseKeyPair`(RSA 개인키쌍 평문) |
| Derby 드라이버 | License4J 설치폴더 `libs.jar`에 `org.apache.derby` 번들(별도 확보 가능). 프로젝트엔 `license4j-runtime-library-4.7.3.jar`(검증 전용) 이미 존재 |
| 라이브 잠금 | License4J Manager 실행 중엔 Derby 폴더가 단일 점유 잠금 → **사본을 복사해서 읽어야 함** |

### 2-1. "발급 페이지(geonuris 방식)"가 불가한 이유 — 범위에서 제외 확정
- License4J 라이선스 **생성/서명 로직은 난독화된 상용 jar**(`com/license4j/*`) 안에 있고, 라이선스가 라이선스 서버 환경(IP·HWID)에 묶여 있다.
- License4J가 공식 제공하는 SDK(`license4j-runtime-library`)는 **검증 전용**, 생성 API 없음(자가발급 차단이 상용 제품의 설계 의도).
- 개인키가 DB에 평문 존재해 *암호학적으론* 불가능은 아니나, 난독화 상용 제품 역설계 = 취약(업데이트 시 파손)·회색지대·유지보수 부담 → **하지 않는다.**
- **핵심: 발급은 불필요하다.** 발급은 기존대로 License4J Manager가, SW Manager는 **이미 발급된 `licenseString`을 수확·보관·배포**하는 역할 분담.

---

## 3. 범위

### In Scope
- 매월 말 License4J Derby 데이터를 읽어 대장에 반영(신규/변경분 upsert).
- 반영 직전 `license_registry` 스냅샷 백업.
- 매월 Derby 폴더 백업본 보관(원본 백업 겸용).
- 기존 업로드 기준(§4) 그대로 재사용.

### Out of Scope
- 라이선스 자가 발급/서명 페이지(§2-1).
- License4J 개인키 추출·활용.
- 기존 대장 화면/스키마 변경(요청: "대장 그대로 유지"). 단, 백업 테이블 1개 신설은 포함.
- 라이선스 country/label 수동 한글화 정책 변경(§6 영구패스 준수 — Enum 도입·일괄 한글화 안 함).

---

## 4. 준수해야 할 기존 CSV 업로드 기준 (계약)

`LicenseRegistryService.uploadCsvFile` + `LicenseRegistryController` 분석 결과. **이 기준을 한 줄도 바꾸지 않고 재사용**한다.

1. **컬럼 계약**: 정확히 **63개 컬럼**(`License ID` … `License String`). `downloadCsv()`가 헤더 순서·이름의 정본. 업로드는 헤더명으로 매핑(순서 무관, 이름 정확 일치 필요).
2. **식별/중복키**: **License ID + Product ID** 조합.
3. **갱신 규칙**:
   - 기존행 有 + (`Hardware ID` 또는 `License String` 변경) → 전체 63필드 갱신 + `uploadDate`/`uploadedBy` 갱신.
   - 기존행 有 + 변동 없음 → **중복 스킵**.
   - 기존행 無 → 신규 insert.
4. **파싱**: UTF-8, RFC4180(따옴표/콤마/`""` 이스케이프), 빈 줄 스킵. 정수 빈값·`null`→null, 불리언 `True`/`true`→true, 날짜 `yyyy-MM-dd HH:mm`.
5. **업로드 검증(컨트롤러)**: `.csv` 확장자, MIME 화이트리스트, 10MB, 파일명 경로탐색 차단, **EDIT 권한**.
6. **이력**: `LicenseUploadHistory`(파일명/총건수/성공/실패/업로더/일시) 기록.
7. **만료 계산**: `generationDateTime + validityPeriod일`(0·null=무제한).

### 4-1. 업로드 화면 명시 제약 (필수 — `registry-upload.html` guide-box, 사용자 확정 2026-06-29)
- **C-1 (형식 표준)**: **"LSA Basic 2.5 형식"의 63컬럼 레이아웃**을 표준으로 한다. = LSA Basic 2.5 export 시 나오는 그 63컬럼 형식. **전 제품 적재**(Derby 15제품 모두), 제품 필터 아님. → 매월 생성하는 CSV는 이 63컬럼 헤더·순서와 **정확히 일치**해야 한다.
- **C-2 (중복키)**: License ID + Product ID 동일 시 처리(§4-2,3).
- **C-3 (갱신 동작 — 코드 기준 필수)**: 화면 안내문은 "신규만 등록/중복 스킵"으로 적혀 있으나, **실제 코드의 변경분 갱신 동작이 정본·필수**다(사용자 확정). 즉 기존행이라도 Hardware ID 또는 License String 변경 시 전체 갱신, 완전 동일 시에만 스킵. → 안내문 문구는 후속 정정 대상(코드가 SSoT).
- **C-4 (만료 자동계산)**: Generation Date Time + Validity Period.
- **C-5 (결과 표시)**: 성공/중복/실패 건수.

---

## 5. 설계 방향

### 5-1. 기존 업로드 기준(upsert 코어) 재사용 (핵심 원칙) — D2 확정 반영
> ⚠ v0.3 정정: D2(§10-1)로 **CSV 텍스트 생성·재파싱 방식은 폐기**한다. `licenseString`이 여러 줄 텍스트라 CSV 왕복이 취약(codex 지적 #3)하기 때문.

Derby에서 읽은 데이터로 **`LicenseRegistry` 레코드를 직접 생성**하고, **기존 `uploadCsvFile`의 1건 upsert 로직(중복키 C-2 + 변경분 갱신 C-3 + 이력)을 메서드로 추출한 "공유 upsert 코어"에 투입**한다. 기준 분기·재구현 없음.
- 최소 리팩터: `uploadCsvFile`에서 per-record upsert를 `upsertOne(LicenseRegistry, uploadedBy)` 형태로 추출. **수동 업로드 경로(파서·컨트롤러)는 그대로** 그 코어를 호출(회귀 0), 월간 연동은 Derby 레코드로 같은 코어 호출.
- CSV 텍스트를 만들지 않으므로 줄바꿈/RFC4180 파서 취약성과 무관. 63컬럼 형식(C-1) 준수는 §10-2 **표준 필드 매핑 완전성 검증**으로 보장.

### 5-2. Derby → 63컬럼 매핑 규약
- 데이터 소스: `APP.LICENSES l JOIN APP.PRODUCTS p ON l.productID = p.productID` (컬럼은 quoted mixed-case 식별자 → SQL에서 `"licenseID"` 형태 큰따옴표 필수).
- 직통 매핑(변환 불필요): `License ID`←`licenseID`, `Product ID`←`p.productIDString`, `Generation Date Time`←`generationDateTime`(이미 `yyyy-MM-dd HH:mm` 문자열), `Hardware ID`←`hardwareID`(원본 `&&` 다중값 그대로), 등록자/회사/이메일/유효기간 등.
- **코드→라벨 변환 필요 컬럼**(Derby는 정수 코드): `License Type`(`selectedLicenseType`), `Generation Source`(`generationSource`), `Activation Return`(`selectedActivationReturn`), `Hardware ID Selection`(`selectedHardwareIDSelection`), `Date Time Check`(`selectedDateTimeCheck`). SMALLINT(0/1) 불리언 컬럼 → `True`/`False`.
  - **매핑표 도출 방식**: 기존 `license_registry`(라벨 보유) ↔ Derby(코드 보유)를 같은 `licenseID`로 조인하면 **회사가 실제 써온 정확한 코드↔라벨 대응표**가 그대로 나온다. 추측 금지, 실증 도출. (개발계획 단계에서 매핑표 확정·문서화)

### 5-3. 데이터 접근 경로 (라이브 잠금 회피 + 백업 겸용)
```
[매월 말 스케줄]
 1. SMB로 .188 license4jdb 폴더를 앱서버 임시 경로로 복사   ← 이 복사본 = 그 달의 Derby 백업본
 2. license_registry 스냅샷 백업(§5-4)
 3. 복사본 Derby를 read-only 부팅 → APP.LICENSES⨝PRODUCTS 읽기 → LicenseRegistry 레코드 생성(+표준필드 매핑 완전성 검증 §10-2)
 4. 공유 upsert 코어에 레코드 투입(중복키 C-2 / 변경분 갱신 C-3)
 5. 결과 집계 → license_sync_history(D1) 기록 + 로그, 임시 복사본 정리(백업본은 월별 보관)
```
- 절대 라이브 DB 직결 금지(단일 점유 잠금). 항상 사본.

### 5-4. 백업 (사용자 확정: 옵션 A)
- 반영 **직전** `license_registry` 전체를 스냅샷 백업 테이블(`license_registry_backup`, `snapshot_id`/`snapshot_at` 부여)에 복제. 롤백·이력 추적 가능.
- 보관 정책(N개월) 개발계획에서 확정.

### 5-5. 실행 방식 (확정 2026-06-29)
- **월말 자동 스케줄러** + **관리자(ADMIN) 전용 수동 트리거**(테스트·온디맨드). 결과는 기존 업로드 이력 화면(연동 이력 탭)에서 확인.
- 별도 신규 UI 페이지는 만들지 않음(대장 화면 그대로). 수동 트리거는 최소 관리 액션.

---

## 6. 보안 / 운영 전제

- **운영서버 → 192.168.10.188 SMB(파일) 접근 가능** 해야 함(내부망). 미가능 시 실행 방식 재검토. **← 인프라 확인 필요**
- SMB 자격증명(administrator/비번)은 코드·git에 하드코딩 금지 → 환경변수/시크릿 채널(`docs/AGENT_SAFETY.md`).
- `APP.PRODUCTS.licenseKeyPair`(개인키) 등 민감 데이터는 **읽지도 저장하지도 않는다**(대장 적재에 불필요). Derby 백업본·임시 복사본 접근 권한 최소화.
- 신규 동작은 권한 가드 기본 포함(수동 트리거 = ADMIN).

---

## 7. 요구사항

### 기능 요구 (FR)
- **FR-1** 매월 말 지정 시점에 License4J Derby 데이터를 읽어 대장에 반영한다.
- **FR-2** 반영은 기존 업로드 기준(§4 + §4-1 C-1~C-5)을 그대로 따른다: 63컬럼 형식 정확 일치(C-1, 전 제품), 중복키 License ID+Product ID(C-2), 변경분 갱신(C-3), 만료 자동계산(C-4), 결과 집계(C-5).
- **FR-3** Derby→표준필드 매핑에서 코드형 값은 §5-2 실증 매핑표로 라벨 변환한다(미매핑 코드=실패+경보, §10-2).
- **FR-4** 반영 직전 `license_registry` 스냅샷을 백업 테이블에 저장한다.
- **FR-5** 매월 Derby 폴더 복사본을 백업으로 보관한다.
- **FR-6** ADMIN 수동 트리거로 즉시 동기화를 실행할 수 있다.
- **FR-7** 동기화 결과(총/신규/갱신/중복/실패)를 `license_sync_history`(D1)에 기록하고 화면에서 확인 가능하다. (표시 위치: 기존 업로드 이력 화면에 연동 이력 탭/구분 추가 — 신규 페이지 없음. 구체 필드·배치는 개발계획에서 확정.)

### 비기능 요구 (NFR)
- **NFR-1** 라이브 Derby 직결 금지(항상 사본 read-only).
- **NFR-2** SMB 자격증명·개인키 등 시크릿 비노출(코드/로그/git).
- **NFR-3** 기존 동작 보존: `uploadCsvFile`은 per-record upsert 코어 **추출 리팩터만**(수동 업로드 동작·파서 동일, 회귀 0). `license_registry`·`LicenseUploadHistory` 스키마 불변, 신규는 `license_sync_history`·`license_registry_backup` 추가뿐.
- **NFR-4** §6 영구패스 준수 — license enum 도입·country 일괄 한글화 안 함.
- **NFR-5** 1,567행 규모에서 단일 트랜잭션 부담·타임아웃 고려(배치/청크 여부 개발계획에서).

### 테스트 (T)
- **T-1** Derby 사본 read-only 부팅 + APP.LICENSES⨝PRODUCTS 행수 검증.
- **T-2** 코드→라벨 매핑표가 기존 대장과 일치(샘플 N건 round-trip).
- **T-3** 표준필드 매핑 완전성 검증(preflight) 통과 + 필수필드 누락/미매핑 코드 시 실패 처리, 동일 데이터 재투입 시 전건 "중복 스킵"(idempotent).
- **T-4** Hardware ID/License String 변경 1건 → 갱신 1건만 발생.
- **T-5** 백업 스냅샷 생성·복원 검증.
- **T-6** 수동 트리거 ADMIN 가드(비ADMIN 차단).

---

## 8. 리스크 / 미해결 (승인 시 확정 필요)

1. **수동 한글화 덮어쓰기**: 갱신(변경분) 시 `country`/`company` 등이 Derby 원본(영문)으로 덮어써질 수 있음. 기존 수동 업로드와 **동일 동작**이나 자동화로 빈도↑. → 변경분만 발생하므로 안정 라이선스는 보존되나, 정책 확인 필요(영구패스 S4와 상충 여부).
2. **운영서버→.188 SMB 접근 가능 여부**(§6). 불가 시 (배치를 회사 PC/별도 호스트에서 실행 후 업로드) 등 대안.
3. **Derby 버전 호환**: 앱에 번들할 Apache Derby 드라이버가 10.10 생성 DB를 read-only로 읽는지(소프트 업그레이드) 검증 — 개발계획 T 항목.
4. **매핑표 미싱 코드값**: 기존 대장에 없는 코드값 출현 시 fallback 정책(원시 코드 보존 vs 차단) 필요.
5. **A+D 디자인 정책**: 수동 트리거 UI 최소(`버튼`) 포함 시 디자인팀 자문 트리거 여부 — 판단 필요(현재 신규 페이지 없음 → 경계선).

---

## 9. 승인 요청 항목
- [x] 실행 방식: **월말 자동 스케줄러 + ADMIN 수동 트리거** (§5-5) — 확정 2026-06-29
- [x] 리스크 1(한글화 덮어쓰기) **허용**(기존 업로드와 동일 동작) — 확정 2026-06-29
- [x] 형식 제약 C-1: **63컬럼 표준·전 제품** / 갱신 C-3: **변경분 갱신 유지** — 확정 2026-06-29
- [ ] **§6 운영서버(WAR 구동 호스트) → 192.168.10.188 SMB/파일 접근 가능 여부** — 인프라 확인 필요(유일 미결)
  - 가능 → 앱 내 스케줄러로 직접 실행
  - 불가 → 배치 실행 위치 대안(예: 별도 내부망 호스트에서 읽어 업로드 API 호출) 개발계획에서 설계

---

## 10. codex 1차 검토 반영 (2026-06-29, ⚠수정필요)

codex가 기획서+실제 코드를 대조해 3개 주요 지적 + 5개 권고. 아래로 반영. **2건(D1·D2)은 사용자 결정 완료(2026-06-29).** codex 2차 재검토에서 지적 1·2·3 실질 반영 확인, 잔여는 문서 정합성 정리(v0.3에서 §5·NFR-3·판정요약을 D2 기준으로 정정 반영).

### 10-1. 사용자 결정 (2026-06-29 확정)
- **D1 (이력 구조) → 확정: 별도 `license_sync_history` 테이블 신설.** 월간 연동 전용 이력(총/신규/갱신/중복/실패/시작·종료시각/소스 스냅샷 등). 기존 `LicenseUploadHistory`·`license_registry`는 불변(NFR-3 최선 보존). 자동/수동 이력 분리.
- **D2 (적재 방식) → 확정: Derby → `LicenseRegistry` 레코드 직접 생성 → 공유 upsert 코어 호출.** `uploadCsvFile`에서 1건 upsert(중복키 C-2 + 변경분 갱신 C-3) 로직을 메서드로 추출해 **수동 업로드(파서)와 월간 연동(Derby 레코드)이 동일 코어 공유**. CSV 텍스트 미경유 → `licenseString` 여러 줄 문제 원천 제거, 타입 안전. 수동 업로드 경로의 기존 파서는 그대로 둠(회귀 0). 63컬럼 형식(C-1)은 레코드 매핑 필드 검증(§10-2 preflight)으로 보장.

### 10-2. 채택(설계 확정 — 개발계획에 명시)
- **표준필드 매핑 완전성 검증(preflight)**(지적 #2): D2는 CSV를 만들지 않으므로 "CSV 헤더 검증"이 아니라 **63개 표준필드 매핑 완전성 검증** — 필수필드 누락, 타입/날짜/불리언 포맷, `License String` 다중라인 보존, C-1 컬럼 계약 대응 여부를 적재 직전 검증. 기존 업로드 로직 불변, **연동 매퍼 쪽**에 둠. (FR-2/T-3 보강)
- **미매핑 코드값 정책**(FR-3): 기존 대장 실증 매핑표에 없는 코드값 출현 시 → **해당 건 실패 처리 + 경보 로그(차단)**, 원시 코드 무단 저장 안 함(데이터 오염 방지). T-2에 케이스 추가.
- **Derby 복사 일관성**(FR-5): 라이브는 앱 점유 → 파일 복사 후 **read-only 부팅 성공 = 일관성 확인** 게이트. 부팅 실패 시 재복사/스킵 + 경보. (가능 시 Derby online backup 우선 검토)
- **로그 마스킹**(NFR-2): SMB 경로/계정/`licenseKeyPair` 등은 로그·예외에 미기록.
- **트랜잭션/롤백**(NFR-5): 청크 단위 처리 + 연동 실패 시 직전 스냅샷(§5-4)으로 롤백 절차 개발계획에 명시.
- **SMB 불가 대안**(FR-1): 별도 내부망 호스트 배치 경로 개발계획 포함(§9).
- **T 보강**: T-3 preflight 실패 케이스 / T-5 스냅샷 실제 롤백 / T-6 스케줄러=시스템계정·수동=ADMIN 구분.

### 10-3. 항목 판정 요약 (v0.4 기준)
- **FR-7**: D1(별도 `license_sync_history`)로 **해소**.
- **FR-2**: D2(레코드 직접+공유 코어) + preflight 재정의(§10-2)로 **충족 가능**.
- **FR-1·3·5**: SMB 접근 확인 / 미매핑 차단 / 복사 일관성 게이트 반영 시 충족(잔여=SMB 인프라 §9 1건).
- **FR-4·6**: 충족 가능.
- **NFR-1·4**: 충족. **NFR-2·3·5**: 로그 마스킹·추출 리팩터·청크/롤백 반영으로 해소.
- **T-1~6**: 적절(보강 반영). 
- codex 2차: 기능 설계·D1·D2·권고 실질 반영 확인, 문서 정합성 정리 완료.
