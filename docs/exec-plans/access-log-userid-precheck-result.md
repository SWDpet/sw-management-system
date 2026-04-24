# access-log-userid-fix Step 1 — 사전검증 결과

- **실행시각**: 2026-04-20 19:50:46 KST
- **DB**: `jdbc:postgresql://211.104.137.55:5881/SW_Dept`
- **안전통제**: READ ONLY, `statement_timeout=10s`
- **러너**: [`access-log-userid-precheck.java`](./access-log-userid-precheck.java)

---

## (a) userid 분포 + status

| userid | cnt | status |
|--------|----:|--------|
| admin | 1423 | valid |
| hanjun | 1013 | valid |
| seohg0801 | 749 | valid |
| ukjin914 | 640 | valid |
| test02 | 36 | valid |
| **박욱진** | **16** | **orphan** ⚠ |
| parksh | 13 | valid |
| **관리자** | **11** | **orphan** ⚠ |
| yeohj | 8 | valid |
| anonymousUser | 4 | anonymous |
| ybkang | 4 | valid |
| jeongsj | 4 | valid |

- 정상 9종 + anonymousUser 1종 + **orphan 2종** (실명이 userid 자리에 들어감)

## (b) 오염값별 username 1:1 매칭 후보

| dirty_userid | candidate_userid | username | log_cnt | match_kind |
|--------------|------------------|----------|--------:|------------|
| `관리자` | `admin` | 관리자 | 11 | **1:1 (auto-update OK)** ✅ |
| `박욱진` | `ukjin914` | 박욱진 | 16 | **1:1 (auto-update OK)** ✅ |

- **두 건 모두 1:1 매칭** — username → userid 변환 자동 가능, 동명이인 없음
- 0:N (삭제된 사용자 흔적) 없음
- 1:N (동명이인) 없음

## (c) 오염 레코드 총량 (UPDATE 기대값)

- **총 오염 레코드: 27건** (= 11 + 16, (a)(b) 합과 일치)
- 참고: legacy `anonymous` 건수: **0건** (이미 anonymousUser 로 통일된 상태)

---

## ✅ 매핑표 (확정안)

| 변경 전 (`access_logs.userid`) | 변경 후 | 영향 row | 근거 |
|--------------------------------|---------|---------:|------|
| `관리자` | `admin` | 11 | `users.username='관리자'` 인 사용자의 userid |
| `박욱진` | `ukjin914` | 16 | `users.username='박욱진'` 인 사용자의 userid |
| **합계** | | **27** | (c) 와 일치 |

## 🚦 사용자 승인 게이트

위 매핑표대로 V019 마이그레이션 SQL 을 작성하여 27건을 UPDATE 합니다.

**확인 사항:**
1. `관리자` 11건 → `admin` 으로 통일 — OK?
2. `박욱진` 16건 → `ukjin914` 으로 통일 — OK?
3. UPDATE 정확히 **27건** 실행 (이외 건수면 EXCEPTION → 자동 ROLLBACK)
4. 백업 테이블 `access_logs_cleanup_backup_<run_id>` 자동 생성

승인하시면 **Step 2 (V019 SQL 작성)** 진행합니다.
