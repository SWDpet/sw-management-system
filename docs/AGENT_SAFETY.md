# 🛡 AGENT_SAFETY — agent 작업 안전 규칙

본 문서는 Claude Code / codex CLI 등 LLM agent 가 본 프로젝트에서 작업할 때 따라야 할 안전 규칙입니다. **모든 agent 작업의 기본 정책**이며, 위반 시 사용자 명시 승인 없이는 진행 금지.

> **누가 적용**: Claude Code, codex CLI, 향후 도입될 다른 agent 모두

---

## §1 DB 작업 read-only 가드

agent 가 DB 와 직접 상호작용할 때:

| 작업 종류 | 자동 진행 | 사용자 승인 필요 |
|----------|----------|------------------|
| `SELECT` (조회) | ✅ | — |
| `EXPLAIN` (쿼리 분석) | ✅ | — |
| `UPDATE` / `DELETE` (데이터 변경) | ❌ | 명시 승인 필수 |
| `INSERT` (신규 데이터) | ❌ | 명시 승인 필수 |
| `DROP` / `TRUNCATE` (스키마/데이터 삭제) | ❌❌ | 2단계 confirm 필수 |
| `ALTER` (스키마 변경) | ❌❌ | 2단계 confirm 필수 |

**예외**: 단위 테스트 (`@SpringBootTest`) 가 자체 ephemeral DB 를 띄우는 경우 read/write 자유. 단 prod DB 좌표를 받아쓰면 안 됨 (§4 참조).

---

## §2 마이그레이션 / DDL 변경

스키마 변경은 항상 **사용자 명시 발화** + **2단계 confirm**:

1. 사용자 발화: "마이그레이션 진행" / "DDL 변경 진행" 등 명확한 의도
2. agent 가 변경 SQL 미리 보기 + 영향 표 추정 + 롤백 SQL 동시 제시
3. 사용자 1회 더 "yes" 발화 후 진행
4. 변경 후 즉시 검증 SQL 실행 + 결과 보고

이 규칙은 NL 트리거 confirm 게이트 (`feedback_trigger_confirm.md`) 와 별도로 적용 — 마이그레이션은 확인 단계가 더 엄격.

---

## §3 Ephemeral test DB 가이드

prod DB 사고 차단 위해 로컬 테스트는 ephemeral container 사용 권장. 예시 `docker-compose.test.yml`:

```yaml
services:
  test-db:
    image: postgres:16
    container_name: swdept-test-db
    environment:
      POSTGRES_DB: swdept_test
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: test_password_only
    ports:
      - "5433:5432"  # 호스트 5433 → 컨테이너 5432 (prod 5881 과 충돌 회피)
    tmpfs:
      - /var/lib/postgresql/data  # 컨테이너 종료 시 데이터 자동 삭제
```

사용:
```bash
docker compose -f docker-compose.test.yml up -d
DB_URL=jdbc:postgresql://localhost:5433/swdept_test \
  DB_USERNAME=postgres \
  DB_PASSWORD=test_password_only \
  ./mvnw test
docker compose -f docker-compose.test.yml down
```

본 스프린트 (`harness-hardening-v1`) 는 가이드만 제시 — 실제 docker-compose 셋업은 사용자 결정.

---

## §4 시크릿 안전 채널

**Git/dev-environments repo 로 동기화 금지** 시크릿:
- `OPENAI_API_KEY`, `ANTHROPIC_API_KEY`
- `DB_PASSWORD`, `DB_USERNAME`
- `*_TOKEN`, `*_SECRET`, `*_KEY` (단 `JAVA_HOME`/`M2_HOME` 등 path 키는 OK)

권장 안전 채널:
- **1Password / Bitwarden** — 다환경 동기화에 가장 적합 (자동 fill, 다단계 인증)
- **OS keychain** (macOS Keychain, Windows Credential Manager)
- **환경변수 매뉴얼 export** (`.env` 파일 — git ignored)

dev-environments repo 의 `home.md`/`office.md` 등에는 **보유 여부만** 기록 (`<MASKED>`):
```
| `OPENAI_API_KEY` | `<MASKED>` | codex CLI 인증 |
```

---

## §5 prod 좌표 마스킹 정책

문서/코드/스크립트의 **운영 DB IP/호스트명** 은 환경변수 placeholder 로만 표기:

❌ Bad: `spring.datasource.url=jdbc:postgresql://211.104.137.55:5881/SW_Dept`
✅ Good: `spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/swdept_local}`

- default 값은 **반드시 dummy** (`localhost:5432/swdept_local`) — 환경변수 미설정 시 prod 사고 차단
- 실제 좌표는 `.env` 또는 1Password
- T-6 grep 규칙 (`harness-hardening-v1` 개발계획 §2) 으로 잔존 검사

---

## §6 위반 시 대응

agent 가 본 규칙 위반 가능성을 인지하면:
1. 즉시 작업 중단
2. 사용자에게 어떤 규칙 어떤 부분이 위반될 수 있는지 보고
3. 사용자 명시 우회 승인 후에만 진행

이는 codex 외부 검증 단계에서도 확인 — codex review 시 prod DB 좌표 노출, 시크릿 commit, 미승인 DDL 등 발견하면 ⚠/❌ 판정.

---

*본 문서는 `harness-hardening-v1` 스프린트로 신설. 이후 발견된 안전 패턴은 본 문서에 누적.*
