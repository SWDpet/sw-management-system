# 운영DB 접속 가이드 — 환경별(내부망/외부망)

> ⚠️ 이 저장소는 **public** 입니다. 이 문서에는 **실제 IP·포트·비밀번호를 절대 넣지 마세요.**
> 실제 좌표는 1Password / 사내 운영팀 / 로컬 `.env`(gitignore) 에서만 관리합니다.

> 🔒 **2026-06-09 정책 변경**: 기존 **외부 공인 IP 접속 경로는 보안 취약으로 차단**되었습니다.
> 운영DB 는 이제 **사무실 내부망에서만** 접속합니다. 집/출장지에서는 DB 직접 접속 불가 → **코드 작업 + GitHub pull 만**.

운영DB(`SW_Dept`)는 **사무실 내부망에서만** 접속합니다. (과거 외부 공인 IP 경로 폐지)

## 접속 경로

| 작업 위치 | DB 접속 |
|-----------|---------|
| **사무실 (사내 내부망)** | ✅ 내부망 호스트 (`INTERNAL_DB_HOST`) — **유일 경로** |
| **집 / 출장지** | ❌ DB 직접 접속 불가(보안 차단). 코드·문서 작업 + `git pull` 만 |

- **DB 명·계정**: `SW_Dept` / `SW_Dept`
- 실제 내부망 호스트·포트는 **1Password / 운영팀** (이 public 저장소엔 절대 미기재)
- **비밀번호**: 환경변수 `DB_PASSWORD` 로만 주입. 코드·문서·커밋에 절대 포함 금지.

> 운영 서버 자기 자신에서 실행할 때만 `localhost` 로 붙습니다. 그 외(사무실 개발 PC)는
> **내부망 호스트**를 써야 하며, 개발 PC 의 `localhost` 는 별개의 로컬 인스턴스이니 혼동 주의.
> (외부 공인 IP 경로는 2026-06-09 보안 차단 — 더 이상 사용 불가)

## 설정 방법 (Spring Boot)

접속 좌표는 모두 **환경변수**로 주입합니다 (`application-*.properties` 는 `${DB_URL}` 등 placeholder 만 보유).

```
DB_URL=jdbc:postgresql://<위치에 맞는 호스트>:<포트>/SW_Dept
DB_USERNAME=SW_Dept
DB_PASSWORD=<비밀번호 — 1Password>
SPRING_PROFILES_ACTIVE=prod   # 운영DB 접속 시
```

실제 호스트/포트/비번 값은:
1. **1Password** (사내 vault) — 1차 소스
2. 로컬 `.env` 또는 `application-local.properties` (둘 다 **gitignore**) 에 복사해 사용
3. OS 사용자 환경변수 (운영 서버·고정 PC) — 자세한 셋업은 [WAR 운영 배포 가이드](exec-plans/archive/quotation-deploy.md) 및 운영팀 문서 참조

운영DB 접속은 **사무실 내부망에서만** 가능합니다 (계정/DB/비번 동일, 외부 경로 폐지).

## psql 로 직접 접속 (점검·핫픽스)

```powershell
$env:PGPASSWORD = '<비밀번호>'      # 1Password. 명령 히스토리에 남지 않게 주의
$env:PGCLIENTENCODING = 'UTF8'      # 한글 깨짐 방지 필수
& "<psql 경로>\psql.exe" -h <위치에 맞는 호스트> -p <포트> -U SW_Dept -d SW_Dept
```

- **한글이 포함된 SQL** 은 인라인(`-c`) 대신 **UTF-8 로 저장한 .sql 파일 + `-f`** 로 실행하세요. 인코딩 경계에서 한글이 깨질 수 있습니다.
- 데이터 변경은 `BEGIN; ... COMMIT;` 트랜잭션으로 감싸고, 변경 전/후 `count` 로 검증하세요.

## 보안 원칙

- 실제 호스트·포트·계정·비번은 **이 public 저장소에 커밋 금지** (1Password / `.env`(gitignore) 만).
- `.env`, `application-local.properties`, `application-prod.properties` 는 `.gitignore` 대상입니다. 커밋되는 건 `*.example` / 본 가이드뿐.
- 비밀번호를 채팅·이슈·커밋 메시지에 남기지 마세요.
