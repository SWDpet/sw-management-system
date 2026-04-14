# 환경 구축 가이드 (신규 PC / 다른 PC 설치 시)

## 필수 체크리스트

### 1. Java 17 (Adoptium) 설치
```
C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot
```
- 환경변수 `JAVA_HOME` 설정 필수

### 2. PostgreSQL 접속 정보
`src/main/resources/application-local.properties` 파일을 직접 생성 (Git에 포함되지 않음)

```properties
server.port=9090
spring.datasource.url=jdbc:postgresql://호스트:포트/DB명
spring.datasource.username=사용자명
spring.datasource.password=비밀번호
```

### 3. ⚠️ GeoNURIS_License.jar 파일 배치 (중요!)

**이 파일은 Git에 포함되지 않습니다. 반드시 수동으로 배치해야 합니다.**

```
경로: src/main/resources/geonuris/GeoNURIS_License.jar
```

#### 파일을 받을 수 있는 곳
- 기존 운영 서버에서 복사
- 사무실 PC의 `src/main/resources/geonuris/` 디렉토리
- 라이선스 관리자에게 요청

#### 파일 없이 실행하면?
- 서버는 정상 기동됨
- **GeoNURIS 라이선스 발급 메뉴에서 실행 시** 아래 에러 발생:
  > 발급 실패: GeoNURIS_License.jar 없음.

### 4. 빌드 및 실행
```bash
# Maven 빌드 (pre-build check에서 jar 파일 확인)
./mvnw clean package -DskipTests

# 로컬 실행
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

---

## 자동 방지 장치 (이 저장소에 적용됨)

### Maven Enforcer 빌드 전 체크
`pom.xml`에 아래 플러그인이 설정되어 있어 **빌드 시 파일이 없으면 명확한 에러 메시지**를 출력합니다.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-enforcer-plugin</artifactId>
    ...
</plugin>
```

이 장치가 있어야 신규 PC에서 파일 누락 시 애매한 런타임 에러 대신 빌드 단계에서 즉시 파악 가능합니다.

---

## 향후 방지 방안 (선택)

### (1) 파일을 Git에 포함 — 가장 간단 (권장)
- GitHub 저장소가 Private이라면 안전하게 커밋 가능
- 모든 PC에서 `git pull` 한 번으로 즉시 실행 가능
- **단점:** 바이너리 파일이 Git에 포함되어 저장소 용량 증가

### (2) Git LFS (Large File Storage)
- 바이너리 파일 전용 저장 방식
- Git 저장소 용량 최적화
- 설정 필요 (`git lfs install`, `git lfs track "*.jar"`)

### (3) 사내 파일 서버 / 네트워크 드라이브
- 공유 경로(`\\fileserver\geonuris\`)에 파일 배치
- `application-local.properties`에서 절대경로 지정
  ```properties
  geonuris.license.jar.path=\\\\fileserver\\geonuris\\GeoNURIS_License.jar
  ```
- **장점:** Git과 완전히 분리, 버전 관리 용이
- **단점:** 네트워크 연결 필요

### (4) 현재 방식 유지 + 문서화
- 파일은 각 PC에 수동 배치
- 이 가이드 문서로 실수 방지
- Maven pre-build 체크로 빠른 에러 감지
