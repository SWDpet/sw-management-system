# BOM 변경표 — Spring Boot 3.2.1 → 3.5.16 (P1 산출물)

> 2026-07-01 · `dependency:tree` before/after 대조 · CVE 대응 스프린트

parent 상향만으로 아래 관리버전이 일괄 변경됨(CVE 해소 근거). 모두 **BOM 관리**(pom 명시 없음).

| 라이브러리 | before (3.2.1) | after (3.5.16) | 관련 critical CVE |
|---|---|---|---|
| org.springframework:spring-core | 6.1.2 | **6.2.19** | CVE-2026-41855(9.8) |
| org.springframework:spring-web | 6.1.2 | **6.2.19** | CVE-2026-41855(9.8) |
| org.springframework.security:spring-security-core | 6.2.1 | **6.5.11** | CVE-2024-22234/2026-22748 |
| org.hibernate.orm:hibernate-core | 6.4.1.Final | **6.6.53.Final** | (회귀 주의: JPA 검증) |
| org.thymeleaf:thymeleaf | 3.1.2.RELEASE | **3.1.5.RELEASE** | CVE-2026-40477/40478(9.0) |
| com.fasterxml.jackson.core:jackson-databind | 2.15.3 | **2.21.4** | CVE-2026-54512~5, 2023-35116 |
| org.postgresql:postgresql | 42.6.0 | **42.7.11** | CVE-2024-1597(9.8, SQLi) |
| org.apache.tomcat.embed:tomcat-embed-core | 10.1.17 | **10.1.55** | **11건**(9.0~9.8) |
| org.junit.jupiter:junit-jupiter-api | 5.10.1 | **5.12.2** | (테스트 인프라) |
| org.mockito:mockito-core | 5.7.0 | **5.17.0** | (테스트 인프라) |
| org.testcontainers | 1.19.3 | **1.21.4** | (테스트 인프라) |

## 검증
- **P1 회귀**: `clean test` → **1638 tests green, 0 실패**(3.2→3.5 3-minor 점프 무회귀).
- 명시버전(poi/jfreechart/pdfbox)은 P2 에서 별도 처리(BOM 미관리).
- tomcat/spring/postgresql/thymeleaf/jackson 등 **critical 대다수가 이 표로 해소** — P4 재스캔으로 최종 확인.
