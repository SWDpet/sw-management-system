# 기획·개발계획서 — MainController 대시보드 모델 Map→타입(projection IF + @Getter DTO) (dashboard-typed-model)

- **상태**: v0.1 (정식 — 디자인/DB 자문 포함, codex 검토 + 사용자 최종승인 대기)
- **작성일**: 2026-06-22
- **맥락**: §6-4 최대 남은 클러스터. MainController.buildDashboardModel 의 `Map<String,Object>` 모델 속성 + SwProjectRepository 의 projection 쿼리 2종을 타입화. **UI(대시보드) 결합** → 정식 절차(디자인+DB 자문, 브라우저 QA).
- **⚠ 성격**: 이전 record 스프린트와 다름. **Thymeleaf 결합** — 템플릿이 `${row.compCnt}`/`${r.h}`/`${m.w}` 처럼 **getter(getXxx)** 를 통한 속성접근. **본 스프린트는 백지 렌더 리스크 방지 위해 getter 보장 타입만 사용**(projection 인터페이스 + Lombok @Getter DTO). record 는 accessor 가 getXxx 가 아니라 본 스프린트에서 미사용(codex: "프레임워크 일반 불가"보다 "getter 보장 타입만" 정책으로 표현).
- **projection getter 타입**: wrapper(`Long getTotalCnt()` 등) 권장(primitive 회피, null 안전). DTO 필드명=템플릿 property명 정확 일치 필수(dday→getDday, sysNmEn→getSysNmEn).

## 🎨 디자인팀 가상 자문 (AGENTS.md A+D — 대시보드 UI 터치)
- **시각 변경 없음**: 본 작업은 모델 데이터 형태(Map→타입)만 변경, 템플릿 마크업·CSS·토큰·레이아웃 **무변**. 렌더 결과 픽셀 동일이 목표.
- **백지 함정 주의**(메모리 reference_thymeleaf_*): record accessor 미해결로 인한 백지 → **getter 보유 타입만** 사용. 200 OK ≠ 정상렌더 → **브라우저 실렌더 QA 필수**.
- **stale 정적파일 주의**(메모리): 검증 시 클린 재기동 + Ctrl+F5.
- 자문 결론: 데이터-형태 리팩터(시각 불변)이므로 디자인 토큰/컴포넌트 변경 없음. 단 **렌더 회귀 방지 위해 브라우저 QA 를 수용기준에 포함**.

## 🗄️ DB팀 가상 자문
- 쿼리 결과 형태만 Map→projection 인터페이스. **SQL·집계·GROUP BY·정렬 무변**(JPQL alias 그대로 → projection getter 매핑). 인덱스/마이그레이션 무관.

## Thymeleaf 접근 필드 전수 매핑 (백지 방지 — 모든 접근에 getter 보장)
| 모델 | 템플릿 접근 | 타입 |
|---|---|---|
| `stats`·`topAmount` | sysNm,sysNmEn,totalCnt,compCnt,progCnt,sumCont,sumSw,sumCnslt,sumDb,sumPkg,sumDev,sumHw,sumEtc | **`SystemStatRow`**(JPQL projection IF, getSysNm…getSumEtc) |
| `yearCounts` | y,c,h | **`DashYearBar`**(@Getter: Integer y,long c,int h) ← countByYear projection(`YearCountRow` getY/getC) + h 계산 |
| `logTrend` | date,act,visitors,h | **`DashLogBar`**(@Getter: Object date,long act,long visitors,int h) |
| `menuTop` | menu,cnt,w | **`DashMenuBar`**(@Getter: Object menu,long cnt,int w) |
| `actionCounts` | action,cnt,w | **`DashActionBar`**(@Getter: Object action,long cnt,int w) |
| `upcoming` | title,date,dday | **`DashUpcoming`**(@Getter: String title,LocalDate date,long dday) |
| `expiring` | name,type,days | **`DashExpiring`**(@Getter: String name,String type,long days) |
| `recentProjectLogs` | (AccessLog 엔티티) | 무변 |
| KPI/합계(totalProjects 등) | 스칼라 model attr | 무변 |

- **신규 타입 8**: projection IF 2(SystemStatRow, YearCountRow) + @Getter DTO 6. 패키지 `com.swmanager.system.dto.dashboard`.
- 컨트롤러: `view`(stats 복사) 제거 → stats 직접 사용. totals 루프는 getter(`r.getCompCnt()` 등) 로 합산(현 `lng(r.get(...))` 동치, null→0 lng 헬퍼 유지). topAmount = `new ArrayList<>(stats)` 정렬(getSumCont). yearCounts/logTrend/menuTop/actionCounts/upcoming/expiring = 각 DTO 리스트(h/w 계산 후 생성).
- 리포지토리: `getSystemStats`→`List<SystemStatRow>`, `countByYear`→`List<YearCountRow>`(JPQL alias=getter명 매핑).

## 목표 (FR/NFR)
- **FR**: MainController(23)+SwProjectRepository(2) `Map<String,Object>` ~25선언 제거 → projection IF 2 + @Getter DTO 6. **선언 211→~186**.
- **NFR**: **렌더 결과 픽셀·값 동일**(KPI·차트·테이블·피드 전부), 정렬·집계·증감 로직 동치, 회귀 0, ratchet tighten. URL 무변.

## 검증 (UI 결합 → 브라우저 QA 필수)
1. 단위: `DashboardModelTest`(신규) — DTO/projection getter 값·계산(h/w 스케일·dday·완료율) 단정. (projection IF 는 인터페이스라 단위는 DTO/계산 위주, 쿼리 매핑은 통합으로.)
2. 통합/렌더: 회사 PC 앱 기동(`DEV_HTTPS_ENABLED=false bash server-restart.sh`, 운영DB 직결) → `/` 대시보드 **Chrome 실렌더 확인**(KPI·연도추이·Top·도넛·피드·로그통계·시스템표 전부 정상, 백지/누락 없음) + **서버 로그 `TemplateProcessingException` 부재 확인**(codex). 클린 재기동+Ctrl+F5.
3. `GOLDEN_RECORD=1 … MapDebtRatchetTest` tighten. `./mvnw test` 전체 green(EndpointInventory·기존 대시보드 테스트 회귀 0).
4. codex 검토(Thymeleaf getter 매핑 누락·projection alias·계산 동치·렌더 회귀 리스크).

## 롤백
원자 1 커밋 → `git revert`. 타입 8 신규 + 컨트롤러 buildDashboardModel 재작성 + 리포지토리 2 반환타입 + 템플릿 무변.

## 커밋 메시지(승인 후)
`refactor(api): MainController 대시보드 모델 Map→projection IF/@Getter DTO 8종(Thymeleaf 호환·렌더 불변) + ratchet 211→NNN (§6-4)`
