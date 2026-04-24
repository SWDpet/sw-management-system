# ARCHITECTURE.md — 시스템 아키텍처 개요

> ⚠ **자동 생성 초안 — 검증 필요**
> 본 문서는 `pom.xml` + `src/main/java` 패키지 트리 + `docs/generated/erd.md` 요약 기반 초안입니다. 실제 설계와 불일치하는 부분은 PR 로 수정 요청 주세요.

---

## 1. 기술 스택

| 계층 | 기술 |
|------|------|
| 언어 / JVM | Java 17 |
| 프레임워크 | Spring Boot 3.2.1 (starter-web / thymeleaf / security / data-jpa / validation) |
| 패키징 | WAR (외장 Tomcat or Spring Boot 내장) |
| 뷰 | Thymeleaf + thymeleaf-extras-springsecurity6 |
| DB | PostgreSQL (프로덕션) |
| 기타 | Lombok, SpringDoc, ArchUnit (test), Apache POI (Excel), OpenHTMLToPDF (PDF), ZXing (QR) |
| 빌드 | Maven 3+ (mvnw) |

근거: `pom.xml` dependencies.

---

## 2. 패키지 구조

```
com.swmanager.system/
├── config/                  # Security, Web, JPA, MessageSource 설정
├── constant/enums/          # Enum 도메인 상수
│   ├── AccessActionType     # access_logs.action_type
│   ├── DocumentStatus       # 문서 상태
│   ├── DocumentType         # 문서 유형
│   ├── InspectResultCode    # 점검 결과 (S1)
│   ├── QtCategory           # 견적서 분류 (S8)
│   ├── QuoteTemplateType    # 견적서 템플릿 (S8-C)
│   ├── WorkPlanType / WorkPlanStatus  # 업무계획 (S16)
├── constants/               # MenuName 등 String 상수
├── controller/              # Spring MVC 컨트롤러
├── domain/                  # JPA Entity (최상위)
│   └── workplan/            # 업무계획 하위 도메인
├── dto/                     # DTO / VO
├── exception/               # 사용자 예외 + @ControllerAdvice
├── geonuris/                # GeoNURIS 라이선스 모듈
│   ├── controller/ domain/ repository/ service/
├── i18n/                    # MessageSource Resolver
├── license/                 # 라이선스 레지스트리 모듈
├── qrcode/                  # QR 라이선스
├── quotation/               # 견적서
│   ├── controller/ domain/ dto/ repository/ service/
├── repository/              # Spring Data JPA
│   └── workplan/
├── service/                 # 비즈니스 서비스
├── system/                  # 시스템 그래프 / 인프라 관리
└── util/                    # SensitiveMask, MaskingDetector 등
```

---

## 3. 도메인 모듈 (Top 레벨)

| 모듈 | 대표 Entity / Service | 설명 |
|------|----------------------|------|
| 사업 관리 | `SwProject` / `SwService` | 프로젝트(사업) 마스터 |
| 인프라 | `Infra`, `InfraServer`, `InfraSoftware` | 서버·SW 자산 |
| 점검 | `InspectReport`, `InspectCheckResult`, `InspectTemplate` (+ `check_section_mst`, `check_category_mst`) | 문서관리 점검내역서 (S1, S10) |
| 문서 | `Document`, `DocumentDetail`, `DocumentHistory`, `DocumentSignature` | 착수계/기성계/준공계 등 |
| 업무계획 | `WorkPlan` (+ `work_plan_type_mst`, `work_plan_status_mst`) | 월별 업무 캘린더 (S16) |
| 견적서 | `Quotation`, `QtCategory`, `RemarksPattern` | 견적서 발행 (S3/S8/S8-C) |
| 사용자·권한 | `User`, `CustomUserDetails`, `AuthLevel`, `OrgUnit` | 인증·권한 |
| 로그 | `AccessLog`, `LogService` | 접근 로그 (S5/S9) |
| 담당자 | `PersonInfo`, `PsInfo` | 고객·내부 담당자 |
| 라이선스 | `LicenseRegistry`, `GeonurisLicense`, `QrLicense` | 라이선스 이력 |

---

## 4. 데이터 플로우 (대표)

```
[Browser] → [Controller] → [Service] → [Repository / JpaTemplate] → [PostgreSQL]
                                   ↓
                              [DTO / Entity]
                                   ↓
                              [Thymeleaf Template]
                                   ↓
                              [HTML Response]
```

- 인증: Spring Security + CustomUserDetails + AuthLevel 체크
- 마스킹: `SensitiveMask.java` + `MaskingDetector` (주민번호·전화번호 등)
- 접근 로그: `LogService.log(MenuName, AccessActionType, detail)` (S9 Enum 기반)

---

## 5. DB 스키마 요약

상세 ERD는 `docs/generated/erd.md` 참조.

**주요 테이블 (완료 스프린트 기준)**:
- `sw_pjt`, `ps_info`, `users`, `sigungu_code`, `prj_types`, `sys_mst`
- `inspect_report`, `inspect_check_result`, `inspect_visit_log`, `inspect_template`
- `check_section_mst` (9행, S1), `check_category_mst` (16행, S10)
- `tb_document`, `tb_document_signature`
- `tb_work_plan`, `work_plan_type_mst` (10행, S16), `work_plan_status_mst` (7행, S16)
- `qt_quotation`, `qt_category_mst` (3행, S8), `qt_remarks_pattern`
- `access_logs`, `tb_process_master`, `tb_service_purpose`

**마이그레이션 이력**: `swdept/sql/V017_*.sql` ~ `V026_*.sql`

---

## 6. 빌드·배포

- 개발: `./mvnw spring-boot:run` 또는 `bash server-restart.sh`
- 배포: WAR 패키징 후 Tomcat 배포 (자세한 가이드: `docs/exec-plans/archive/quotation-deploy.md`)
- 설정: `application.properties` (프로덕션: DB_PASSWORD 환경변수)

---

*Last updated: 2026-04-24 · docs-renewal-01 P1 자동 생성 초안*
