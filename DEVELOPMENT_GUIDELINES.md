# 개발 최우선 지침 (DEVELOPMENT GUIDELINES)

> **이 문서는 본 프로젝트의 모든 개발 작업에 적용되는 최우선 규칙입니다.**
> **어떠한 상황에서도 아래 지침을 위반해서는 안 됩니다.**

---

## 🔴 절대 원칙 (ABSOLUTE RULES)

### 1. 원본 소스 무변경 원칙
- **기존 파일의 소스코드를 절대 수정하지 않는다.**
- 기존 `.java`, `.html`, `.properties`, `.xml` 파일의 내용을 변경하거나 삭제하지 않는다.
- 새로운 기능은 반드시 **새로운 파일**을 추가하는 방식으로만 개발한다.
- 단, 연동해야하는 부분에서는 수정하지만 기존 기능이 안되는 상황을 만들면 안된다.

### 2. 기존 기능 보호 원칙
- 새로운 기능 추가로 인해 **기존 기능이 변질되거나 동작하지 않는 일이 절대 발생해서는 안 된다.**
- 기존 URL 경로, API 응답, 데이터베이스 테이블 구조를 변경하지 않는다.
- 기존 Spring Security 설정, 세션 관리, 권한 체계를 훼손하지 않는다.

### 3. 분리 개발 원칙
- 새로운 기능은 독립된 패키지 하위에 구현한다. (예: `com.swmanager.system.quotation`)
- 새로운 DB 테이블은 기존 테이블과 **외래 키 의존 없이** 독립적으로 생성한다.
- 새로운 Thymeleaf 템플릿은 별도 폴더에 생성한다. (예: `templates/quotation/`)

---

## 🟡 개발 수칙

### 패키지 구조
```
새 기능 추가 시 아래 구조를 따른다:
com.swmanager.system.{기능명}/
    ├── controller/     컨트롤러
    ├── domain/         JPA 엔티티
    ├── dto/            데이터 전송 객체
    ├── repository/     JPA Repository
    └── service/        비즈니스 로직
```

### DB 작업 규칙
- `spring.jpa.hibernate.ddl-auto=none` 이므로 테이블은 **SQL 스크립트로 직접 생성**한다.
- SQL 스크립트는 `/swdept/sql/` 폴더에 보관한다.
- 기존 테이블에 컬럼 추가, 인덱스 변경 등을 하지 않는다.

### 보안 연동 규칙
- 새로운 URL 경로의 접근 제어는 **별도의 Security 설정 클래스**로 추가한다.
- 기존 `SecurityConfig.java`를 수정하지 않고, `@Order` 어노테이션을 활용하여 체인에 추가한다.
- 기존 권한 체계(authDashboard, authProject 등)와 동일한 패턴으로 새 권한을 추가한다.

### 네비게이션 연동 규칙
- 기존 HTML 템플릿을 수정하지 않고, **Thymeleaf fragment** 또는 **JavaScript**로 메뉴를 추가한다.
- 새로운 페이지는 기존 레이아웃 스타일과 일관성을 유지한다.

---

## 🟢 확인 체크리스트

새 기능 배포 전 반드시 확인:

- [ ] 기존 파일 중 변경된 파일이 없는가?
- [ ] 기존 URL 접속 시 정상 동작하는가? (/, /projects, /infra, /person, /admin/users)
- [ ] 기존 로그인/로그아웃이 정상인가?
- [ ] 기존 권한 체계가 그대로 유지되는가?
- [ ] 새로운 기능이 독립 패키지에 구현되었는가?
- [ ] 새로운 DB 테이블이 기존 테이블에 영향을 주지 않는가?

---

## 📌 견적서 모듈 연동 변경 이력

> 아래는 연동을 위해 **최소한으로** 수정한 기존 파일 목록입니다.
> 모든 수정은 "기존 기능 무영향" 원칙을 준수합니다.

| 기존 파일 | 변경 내용 | 영향도 |
|-----------|----------|-------|
| `User.java` | `authQuotation` 필드 추가 + `@PrePersist` 기본값 | 기존 필드 무변경, 새 컬럼만 추가 |
| `main-dashboard.html` | GNB에 견적서 메뉴 링크 추가 (권한 조건부) | 기존 메뉴 무변경 |
| `admin-user-list.html` | 권한 설정 `<select>` 1개 추가 (견적서) | 기존 셀렉트 무변경 |
| `AdminUserController.java` | `approve()`/`update()` 메서드에 `authQuotation` 파라미터 추가 | 기존 파라미터 무변경 |
| `V002_quotation_tables.sql` | `ALTER TABLE users ADD COLUMN auth_quotation` | `ADD COLUMN IF NOT EXISTS` 사용으로 안전 |

---

**최초 작성일:** 2026-03-12
**적용 대상:** swmanager 프로젝트 전체
**위반 시 조치:** 해당 커밋 즉시 롤백
