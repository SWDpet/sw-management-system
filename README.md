# SW 관리 시스템 (SW Management System)

Spring Boot 기반 SW 사업 통합 관리 시스템

## 📋 프로젝트 개요

- **프레임워크**: Spring Boot 3.x
- **빌드 도구**: Maven
- **데이터베이스**: PostgreSQL
- **템플릿 엔진**: Thymeleaf
- **보안**: Spring Security

## 🚀 주요 기능

1. **대시보드**: 시스템별 SW 사업 현황 조회
2. **사업 관리**: 사업 등록, 수정, 삭제, 검색
3. **담당자 관리**: 담당자 정보 관리
4. **서버 관리**: 인프라/서버 정보 관리
5. **라이선스 대장**: CSV 업로드, 검색, 페이징 (10개씩)
6. **사용자 관리**: 회원가입, 승인, 권한 관리
7. **로그 관리**: 모든 작업 로그 기록 및 조회

## ⚠️ 신규 PC 설치 시 필수 확인사항

> **`docs/references/setup-guide.md` 를 반드시 먼저 읽어주세요!**
>
> 특히 `GeoNURIS_License.jar` 파일은 Git에 포함되지 않아 수동 배치가 필요합니다.
> 파일 누락 시 `mvn package` 실행 중 빌드가 중단되며 안내 메시지가 출력됩니다.

## 🛠️ 개발 환경 설정

### 필수 프로그램

1. **JDK 17 이상**
2. **PostgreSQL 15 이상**
3. **Eclipse IDE** (또는 IntelliJ IDEA)
4. **Git**

### 필수 파일 (Git 제외)

1. **`src/main/resources/application-local.properties`** - DB 접속 정보
2. **`src/main/resources/geonuris/GeoNURIS_License.jar`** - GeoNURIS 라이선스 발급 모듈

### 데이터베이스 설정

```sql
-- 데이터베이스 생성
CREATE DATABASE swmanager;

-- 사용자 생성 (선택사항)
CREATE USER swmanager_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE swmanager TO swmanager_user;
```

### application.properties 설정

```properties
# 각 환경별로 application-{profile}.properties 파일 생성
# application-local.properties (로컬 개발)
# application-dev.properties (개발 서버)
# application-prod.properties (운영 서버)

spring.datasource.url=jdbc:postgresql://localhost:5432/swmanager
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
```

## 🏃 실행 방법

### Eclipse에서 실행

1. 프로젝트 import
2. Maven → Update Project
3. Run As → Spring Boot App

### 명령어로 실행

```bash
# Maven 빌드
mvn clean install

# 실행
mvn spring-boot:run

# 특정 프로파일로 실행
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## 📦 Git 워크플로우

### 최초 Clone

```bash
git clone https://github.com/your-username/sw-management-system.git
cd sw-management-system
```

### 작업 시작 전 (Pull)

```bash
git pull origin main
```

### 작업 후 (Commit & Push)

```bash
git add .
git commit -m "작업 내용 설명"
git push origin main
```

## 🔑 주요 URL

- **메인 페이지**: http://localhost:8080/
- **로그인**: http://localhost:8080/login
- **관리자 페이지**: http://localhost:8080/admin/users
- **로그 관리**: http://localhost:8080/admin/logs
- **라이선스 대장**: http://localhost:8080/license/registry/list

## 👥 권한 체계

- **ROLE_ADMIN**: 전체 관리자
- **authDashboard**: 대시보드 조회/편집 권한
- **authProject**: 사업 관리 권한
- **authPerson**: 담당자 관리 권한
- **authInfra**: 서버 관리 권한
- **authLicense**: 라이선스 대장 권한

각 권한: NONE (없음), VIEW (조회), EDIT (편집)

## 📝 개발 히스토리

- 2026-03-06: 라이선스 대장 페이징 기능 추가 (10개씩)
- 2026-03-06: 사용자 관리 검색 및 페이징 기능 추가
- 2026-03-06: 라이선스 대장 로그 기록 기능 추가
- 2026-03-06: 메인 페이지 담당자 관리 + 아이콘 추가

## 🐛 문제 해결

### Port 8080 이미 사용 중

```properties
# application.properties
server.port=8081
```

### PostgreSQL 연결 오류

1. PostgreSQL 서비스 실행 확인
2. 포트 번호 확인 (기본: 5432)
3. username/password 확인

## 📞 문의

프로젝트 관련 문의: your-email@example.com
