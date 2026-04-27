# 운영DB Schema Snapshot Meta — 2026-04-27

본 문서는 `phase1-ddl-formalization` 스프린트 FR-0 의 운영DB schema dump 추출 결과 메타데이터입니다.

## 1. 추출 환경

| 항목 | 값 |
|------|---|
| 추출 시점 | 2026-04-27 15:59 KST |
| 추출 실행자 | 박욱진 (ukjin55@gmail.com) |
| 운영DB 호스트 | 192.168.10.194:5880 (사내망, REDACTED in dump) |
| 운영DB 이름 | SW_Dept (REDACTED in dump) |
| 추출 계정 | `ro_phase1_audit` (Step 1 신설, 임시 비밀번호, `default_transaction_read_only=on`) |
| 운영DB 버전 | **PostgreSQL 16.11** (compiled by Visual C++ build 1944, 64-bit) |
| 클라이언트 버전 | psql / pg_dump 16.11 (사용자 PC 로컬) |

## 2. dump 명령

```
pg_dump --schema-only --no-owner --no-privileges \
        --no-comments --no-security-labels --no-tablespaces \
        -h 192.168.10.194 -p 5880 -U ro_phase1_audit -d SW_Dept
```

## 3. dump 파일 정보

| 항목 | 값 |
|------|---|
| 파일 경로 | `docs/references/snapshots/2026-04-27-prod-schema.sql` |
| 파일 크기 | 99,727 bytes (마스킹 후) |
| 라인 수 | 3,758 |
| SHA256 (마스킹 후) | `f3b2b51a12a0e1652636fb5e46a95bd6cf13e0bdc5924394cac8599b24cd3ecb` |
| 마스킹 처리 | `192.168.10.194` → `REDACTED_HOST`, `SW_Dept` → `REDACTED_DB` (실제 dump 본문에는 둘 다 미포함이었으나 안전 차원에서 sed 실행) |

## 4. 환경 스냅샷 (FR-0-E)

| 파일 | 내용 |
|------|------|
| `pg-version.txt` | `SELECT version()` + `SHOW server_version` |
| `pg-settings.txt` | 핵심 8개 GUC (server_version, server_encoding, lc_collate, lc_ctype, TimeZone, standard_conforming_strings, default_transaction_isolation, shared_preload_libraries) — 6개 반환 (lc_collate/lc_ctype 미반환, ro 권한상 별도 확인 필요) |
| `pg-extensions.txt` | `pg_extension` 전체 — `plpgsql 1.0` 만 |
| `pg-collations.txt` | `pg_collation` 상위 20 (대부분 ICU 기반, 153.14.* 버전) |
| `xact-before.txt` | dump 직전 `pg_stat_database` xact_commit (T10 베이스라인) |
| `xact-after.txt` | dump 직후 `pg_stat_database` xact_commit (T10 검증) |

## 5. 검증 매트릭스 결과 (Step 1~2)

| ID | 항목 | 결과 |
|----|------|------|
| **T0** | ro DSN 강제 검사 (UPDATE 차단) | ✅ "읽기 전용 트랜잭션에서는 UPDATE 명령을 실행할 수 없습니다." |
| **T1** | 읽기전용 롤 설정 | ✅ `default_transaction_read_only=on` + `search_path=public` |
| **T2** | dump 무결성 (DML 0건) | ✅ DML 라인 수 = 0 |
| **T3** | dump 자격증명·메타 미포함 | ✅ 호스트명·DB명 dump 본문에 없음 (pg_dump 6플래그 효과), 안전 차원 sed 마스킹 추가 |
| **T10** | 운영DB 쓰기 0 | ✅ xact_commit 변화량 = **28** (기대 ≤ 50) |

## 6. 운영DB 핵심 환경 (phase1.sql 작성에 반영)

- **인코딩**: UTF8
- **타임존**: Asia/Seoul
- **격리 수준**: read committed (기본)
- **확장**: plpgsql 만 (특수 확장 불필요 → ephemeral 검증 시 추가 EXTENSION 안 만들어도 됨)
- **트리거 함수 발견**: `fn_update_timestamp()` — 본 함수가 phase1 의 일부 테이블에서 사용된다면 phase1.sql 에 포함 필요 (Step 4 분석 시 결정)

## 7. 후속 단계

- Step 3 (본 문서) 완료 ✅
- Step 4: dump 분석 → `db_init_phase1.sql` 19테이블 작성
- Step 5: sigungu_code data-only 추출 → `db_init_phase1_sigungu.sql`
- 이하 Step 6~10 순차 진행
