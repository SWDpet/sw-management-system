# 운영DB 접속 가이드 — 환경별(내부망/외부망)

> ⚠️ 이 저장소는 **public** 입니다. 이 문서에는 **실제 IP·포트·비밀번호를 절대 넣지 마세요.**
> 실제 좌표는 1Password / 사내 운영팀 / 로컬 `.env`(gitignore) 에서만 관리합니다.

운영DB(`SW_Dept`)는 **하나의 논리적 DB** 이지만, 접속하는 위치에 따라 **두 개의 네트워크 경로**로 붙습니다. 두 경로는 같은 DB 를 가리킵니다.

## 환경별 접속 경로

| 작업 위치 | 네트워크 | 접속 대상 |
|-----------|----------|-----------|
| **사무실** | 사내 **내부망** | 내부망 호스트 (`INTERNAL_DB_HOST`) |
| **집 / 출장지** | 인터넷 **외부망** | 외부망 호스트 (`EXTERNAL_DB_HOST`) |

- 두 경로 모두 **DB 명·계정은 동일**: `SW_Dept` / `SW_Dept`
- 포트는 경로별로 다를 수 있음 (실제 값은 1Password 참조)
- **비밀번호**: 환경변수 `DB_PASSWORD` 로만 주입. 코드·문서·커밋에 절대 포함 금지.

> 운영 서버 자기 자신에서 실행할 때만 `localhost` 로 붙습니다. 그 외(개발 PC·노트북)는
> 위 표대로 **내부망/외부망 호스트**를 써야 하며, 개발 PC 의 `localhost` 는 별개의 로컬 인스턴스이니 혼동 주의.

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

위치를 옮길 때 **`DB_URL` 의 호스트만** 내부망↔외부망으로 바꾸면 됩니다 (계정/DB/비번 동일).

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
