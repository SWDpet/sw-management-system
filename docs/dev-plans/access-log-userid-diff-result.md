# V019 사후 Diff 리포트

- **run_id**: `20260420_200859`
- **백업 테이블**: `access_logs_cleanup_backup_20260420_200859`
- **실행시각**: 2026-04-20 20:08:59 KST
- **NOTICE**: `PASS: cleaned 27 rows (exact match)`

---

## 백업 (변경 전) 분포

| userid | cnt |
|--------|----:|
| 박욱진 | 16 |
| 관리자 | 11 |
| **합계** | **27** |

## 현재 access_logs 매핑 결과 검증

| 검증 항목 | 결과 |
|----------|----:|
| orphan 잔존 (`박욱진`/`관리자`) | **0** ✅ |
| `admin` 현재 건수 (이전 1423 + 관리자 11 흡수) | **1434** ✅ |
| `ukjin914` 현재 건수 (이전 640 + 박욱진 16 흡수) | **656** ✅ |
| 백업 총 row | **27** |

## 멱등성 검증

재실행(run_id=`20260420_200917`):
```
PASS: cleaned 0 rows (exact match, backup=access_logs_cleanup_backup_20260420_200917)
orphan 잔존: 0 ✅
백업 테이블: rows=0
```

→ ✅ 멱등성 보장 (`WHERE userid NOT IN (SELECT userid FROM users)` 로 재실행 시 0건)

---

## 백업 테이블 목록

| 테이블명 | row | 비고 |
|---------|----:|------|
| `access_logs_cleanup_backup_20260420_200859` | 27 | 1차 실행 (실데이터) |
| `access_logs_cleanup_backup_20260420_200917` | 0 | 멱등성 검증용 (빈 백업) |

빈 백업 테이블(`_200917`) 은 운영 안정 확인 후 DROP 가능. 실데이터 백업(`_200859`) 은 보존 기간 정책 따름.

---

## 추가 증빙 (codex 검토 보강)

### T11 / NFR-1 — `./mvnw clean compile`
```
[INFO] BUILD SUCCESS
[INFO] Total time:  10.823 s
[INFO] Finished at: 2026-04-20T20:13:16+09:00
```

### T4-B — 동일 RUN_ID 재실행 시 HALT 검증
```bash
$ apply.java V019.sql 20260420_200859  # 이미 사용한 run_id
[V019] ❌ FAILED: 오류: HALT: backup table access_logs_cleanup_backup_20260420_200859 already exists
       Where: PL/pgSQL 함수 "inline_code_block" 의 4번째 RAISE
[V019] SQLState: P0001
```
→ ✅ V019 SQL §(0) 게이트가 동명 백업 테이블 존재 시 **즉시 EXCEPTION 발생** (롤백 신뢰성 보호)

### T10 / NFR-5, NFR-6 — 회귀 스모크 (로그인/메뉴 접근)
```
GET /login           → HTTP 200  ✅ (로그인 페이지 정상)
GET /projects/status → HTTP 302  ✅ (인증 필요 리다이렉트, Spring Security 정상)
```
→ 서버 정상 부팅 + 인증 흐름 무회귀

