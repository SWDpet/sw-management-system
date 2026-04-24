# [기획서] 시스템 관계도 — A. 인프라 구성도

- **작성팀**: 기획팀
- **작성일**: 2026-04-18
- **선행**: `af2a1b0`(C 본체), `a0941cd`(C 설명) — 같은 `/admin/system-graph` 위에 A 탭 구현
- **상태**: v2 (codex 재검토 대기) — 1차 검토 4건 반영

### 개정 이력
| 버전 | 변경 |
|------|------|
| v1 | 초안 |
| v2 | codex 검토 반영: ①"초기 30개 선표시" 시나리오 삭제(전체 로드로 통일) ②엔드포인트 표기를 `/admin/system-graph/api/infra` 로 전구간 통일 ③FR-3: Projection/DTO 쿼리 사용으로 민감 필드 조회 단계 배제 명시 ④NFR-4: 단일 요청당 SQL 쿼리 수 ≤ 3 상한 + SQL 로그 검증 추가 |

---

## 1. 배경 / 목표

### 배경
C 탭(ERD) 은 **스키마 구조** 를 보여주는 반면, A 탭은 **실제 운영 중인 인프라 구성**을 시각화해야 한다. 현재 `tb_infra_master` / `tb_infra_server` / `tb_infra_software` 테이블에 데이터가 축적되어 있지만, 관리자가 한눈에 **지자체 → 시스템 → 서버 → 소프트웨어** 계층을 확인할 수단이 없다. 개별 페이지를 일일이 드릴다운해야 한다.

### 목표
- A 탭에서 **계층 트리** 형태의 인프라 구성도를 제공.
- 계층: **지자체(city·dist) → 시스템(tb_infra_master) → 서버(tb_infra_server) → 소프트웨어(tb_infra_software)**
- 실시간 DB 조회 (C 와 달리 정적 파일 기반 아님).
- 필터: 지자체(시·도/시·군·구), 인프라 유형(UPIS/KRAS 등).
- 노드 클릭 → 상세 패널 (타입별 속성, **민감정보 제외**).
- 민감 필드(비밀번호 등) 는 서버 측 DTO 에서 아예 제외.

---

## 2. 사용자 시나리오

1. 관리자가 `/admin/system-graph` → A 탭 클릭.
2. 로딩 스피너 표시 후 인프라 트리 전체가 네트워크 그래프로 렌더링됨.
3. 노드 색상으로 레벨 구분:
   - 지자체(도시): 진한 남색
   - 시스템(infra_master): 파랑
   - 서버(server): 초록
   - 소프트웨어(software): 연노랑
4. **상단 필터**:
   - 시·도 드롭다운 (중복 없는 city_nm)
   - 시·군·구 드롭다운 (선택된 시·도 기준으로 종속 필터링)
   - 인프라 유형 체크박스 (UPIS / KRAS / 기타)
5. 시·도 선택 시 해당 지역 하위 구조만 표시.
6. 노드 클릭:
   - 시스템 노드: `sys_nm / sys_nm_en / infra_type / city_nm·dist_nm`
   - 서버 노드: `server_type / ip_addr / os_nm / server_model` (비밀번호 제외)
   - 소프트웨어 노드: `sw_category / sw_nm / sw_ver / port / install_path` (비밀번호 제외)
7. 마우스 휠 확대/축소, 드래그 팬 (C 와 동일 상호작용).

---

## 3. 화면 / UI 흐름

### 3-1. 레이아웃

```
┌─ 필터 바 ───────────────────────────────────────────┐
│ 시·도[▼] 시·군·구[▼] 유형[☑UPIS ☑KRAS] [새로고침]   │
├─ 네트워크 (좌) ─────────────────┬─ 상세 (우) ──────┤
│                                 │ 선택 노드 정보    │
│  ● 서울특별시                   │ ─────            │
│  ├─ ● 강남구 UPIS               │ (클릭 시 표시)   │
│  │   ├─ ■ WEB 서버              │                  │
│  │   │   └─ □ Tomcat 9.0 (8080)│                  │
│  │   └─ ■ DB 서버               │                  │
│  │       └─ □ Oracle 19c (1521)│                  │
│  └─ ...                         │                  │
└─────────────────────────────────┴──────────────────┘
```

### 3-2. 색상 팔레트 (레벨별)

| 레벨 | 색상 | 코드 | 모양 |
|------|------|------|------|
| 지자체(도시) | 진한 남색 | `#1a237e` | `box` |
| 시스템 | 파랑 | `#4e73df` | `box` |
| 서버 | 초록 | `#1cc88a` | `ellipse` |
| 소프트웨어 | 연노랑 | `#f6c23e` | `ellipse` (작게) |

### 3-3. 엣지
- 지자체 → 시스템 / 시스템 → 서버 / 서버 → 소프트웨어 방향성 있음.
- 레이블 없음 (계층 관계는 위치·색상으로 충분).
- 물리 시뮬레이션(`barnesHut`) 으로 자동 배치 후 stabilize 시 정지.

### 3-4. 상세 패널 노드별 속성

| 레벨 | 표시 필드 | 제외 필드 |
|------|-----------|-----------|
| 지자체 | city_nm, dist_nm, 시스템 개수 | — |
| 시스템 | sys_nm, sys_nm_en, infra_type, 서버 개수 | org_cd/dist_cd (내부용) |
| 서버 | server_type, ip_addr, os_nm, server_model, serial_no, cpu_spec | **acc_id, acc_pw, mac_addr** (비밀번호·MAC 등 민감) |
| 소프트웨어 | sw_category, sw_nm, sw_ver, port, install_path, sid | **sw_acc_id, sw_acc_pw** |

---

## 4. 기능 요건 (FR)

| ID | 내용 |
|----|------|
| FR-1 | `GET /admin/system-graph/api/infra` 엔드포인트: 지자체→시스템→서버→소프트웨어 계층을 JSON 으로 반환. ADMIN 권한 필수. |
| FR-2 | 응답 DTO 는 `InfraGraphDTO { nodes: [...], edges: [...] }` 형태. 각 node 는 `id / label / level / domain / attrs(Map)` 구조. |
| FR-3 | **민감 필드 조회 단계에서 배제**: `acc_id`, `acc_pw`, `mac_addr`, `sw_acc_id`, `sw_acc_pw` 는 DTO 에 포함하지 않는다. 실수로 포함되지 않도록 **JPQL `SELECT new InfraGraphDTO(...)` 생성자 projection 또는 Spring Data Projection 인터페이스로 허용 필드만 조회**한다. 엔티티 전체(`findAll()`) 로드 방식 금지 — 민감 컬럼이 JVM 메모리에도 들어오지 않게 보장. 선택된 필드는 섹션 3-4 표의 "표시 필드" 항목. |
| FR-4 | 지자체 노드 ID 는 `city:{city_nm}|{dist_nm}` 형식. 시스템 노드 ID 는 `infra:{infra_id}`. 서버 `srv:{server_id}`. SW `sw:{sw_id}`. |
| FR-5 | 시스템/서버 노드 라벨은 핵심 필드 요약: 시스템=`sys_nm`, 서버=`server_type (ip_addr)`, SW=`sw_nm sw_ver`. |
| FR-6 | Frontend A 탭이 활성화되면 API 호출 → vis-network 렌더링. 재탭 전환 시 캐시 재사용(수동 새로고침 버튼 제공). |
| FR-7 | 상단 필터: 시·도(중복제거 distinct city_nm), 시·군·구(선택된 시·도 종속), 인프라 유형 체크박스(UPIS / KRAS / 기타). 필터 변경 시 화면 즉시 갱신 (재요청 없이 클라이언트에서 hidden 토글). |
| FR-8 | 노드 클릭 시 상세 패널에 **섹션 3-4 표의 허용 필드만** 표시. 필드명은 한글 라벨로 번역해 제공 (예: `server_type` → `서버 유형`). |
| FR-9 | 설명 필드(XSS 방어): DB 에서 온 모든 문자열은 `escapeHtml()` 경유. 기존 escape 헬퍼 재사용. |
| FR-10 | 초기 로드 시 물리 시뮬 `stabilize` 완료 후 정지 (C 와 동일 UX, FR-11 C 기준). |
| FR-11 | API 응답 실패/빈 데이터 처리: 기존 C 와 동일한 안내 문구 사용 — "인프라 데이터를 불러오지 못했습니다. server.log 확인 필요". |
| FR-12 | 초기 화면에 인프라가 전혀 없는 경우(DB 비어 있음): "등록된 인프라가 없습니다" 안내. 오류 문구와 구분. |

---

## 5. 비기능 요건 (NFR)

| ID | 내용 |
|----|------|
| NFR-1 | API 응답 시간: 인프라 100개 기준 500ms 이내. 구현 전략은 섹션 6 참조 (constructor projection 기반, entity fetch join 금지). |
| NFR-2 | API 응답 크기 상한: 200KB (초과 시 분할 로딩 고려, 이번 범위 밖). |
| NFR-3 | 민감 필드 유출 없음 (FR-3). 검증 2단계: ①SQL 로그에서 SELECT 절에 `acc_pw`/`sw_acc_pw`/`acc_id`/`sw_acc_id`/`mac_addr` 컬럼이 **조회조차 되지 않음** 확인, ②실제 API 응답 바디를 grep 으로 검사해 해당 키워드 출현 0회 확인. |
| NFR-4 | N+1 쿼리 방지: **단일 API 요청당 실행 SQL 쿼리 수 ≤ 3개** (master / server / software 레벨별 1건씩 허용, JOIN 단일 SELECT 도 허용). 검증: `spring.jpa.show-sql=true` 로그 또는 QueryCount 툴로 실제 쿼리 수 확인 → 개발계획서의 회귀 테스트 항목에 "SQL 로그에서 infra 관련 SELECT 3개 이하"로 포함. |
| NFR-5 | 권한: ADMIN only (NFR-6 C 와 동일 SecurityConfig 상속). |
| NFR-6 | DB 스키마 변경 없음. |
| NFR-7 | 기존 C 탭 / 다른 관리자 페이지 동작에 영향 없음. |

---

## 6. DB팀 자문 결과

### 영향받는 테이블
- `tb_infra_master` (읽기)
- `tb_infra_server` (읽기)
- `tb_infra_software` (읽기)

### 쿼리 전략 (FR-3/NFR-3 정합)
- **Entity fetch join 금지** — 엔티티 전체가 메모리에 올라오면 민감 컬럼(`acc_pw` 등) 도 JVM 에 적재되어 FR-3 위반.
- **JPQL constructor projection** 또는 **Spring Data Projection 인터페이스** 로 허용 필드만 선택 조회. 예:
  ```java
  // 옵션 A: JPQL constructor projection
  @Query("SELECT new com.swmanager.system.dto.InfraMasterView(" +
         "  i.infraId, i.infraType, i.cityNm, i.distNm, i.sysNm, i.sysNmEn) " +
         "FROM Infra i")
  List<InfraMasterView> fetchMasters();

  // 서버/SW 는 각각 동일 패턴 (민감 필드 제외한 컬럼만)
  ```
- 총 SELECT 3개 (master / server / software) — NFR-4 상한 내.
- Service 에서 3개 리스트를 받아 메모리에서 지자체→시스템→서버→SW 계층으로 조립.
- 인덱스: `idx_infra_server_infra_id`, `idx_infra_software_server_id` 가 필요. 없으면 DB팀에서 생성 고려(이번 범위 밖 — 성능 문제 발생 시).

### 데이터 변경
**없음** — 읽기 전용.

**DB팀 의견: 승인. 인덱스는 현재 규모(수십~수백 건)에서는 풀스캔도 허용 범위. 데이터 증가 시 재검토.**

---

## 7. 의사결정 / 우려사항

### 7-1. 지자체 노드 표현 — ✅ 확정
- 같은 city_nm + dist_nm 조합이면 하나의 "지자체 노드" 로 묶음.
- 예: "서울특별시 강남구" → 1개 노드, 그 아래 UPIS/KRAS 등 여러 시스템 매달림.

### 7-2. 민감 필드 처리 — ✅ 확정
- 서버 측 DTO 빌더에서 **화이트리스트 방식**으로 명시적 포함. 민감 필드는 코드에서 아예 읽지도 않음.
- 컬럼 단위 필드 목록(섹션 3-4) 을 하드코딩(유지보수 부담 감수). 신규 민감 필드 추가 시 직접 추가 여부 고려 필요.

### 7-3. 필터 전략 — 세부는 개발계획서
- 클라이언트 측 hidden 토글 (간단, 빠름) vs 서버 재호출 (정확, 큰 데이터)
- **권고**: 클라이언트 토글. 현재 규모에서는 전체 로드 후 hide/show 가 충분.

### 7-4. 상세 패널 한글 필드 라벨
- 영문 필드명 → 한글 라벨 매핑은 **프론트 상수 테이블** 로 보관. YAML 까지는 불필요.
- 예: `{"server_type": "서버 유형", "ip_addr": "IP 주소", ...}`

### 7-5. 대용량 데이터 대응 — 이번 범위 밖
- 현재 데이터 소량이라 전체 로드로 충분.
- 추후 100+ 시스템 이상으로 늘면 시·도 선택 후 로드 방식으로 전환 검토.

---

## 8. 영향 범위

| 계층 | 파일 | 유형 |
|------|------|------|
| Backend (신규) | `service/InfraGraphService.java` | 계층 쿼리 + DTO 빌드 (민감 필드 제외) |
| Backend (신규) | `dto/InfraGraphDTO.java` | nodes/edges 응답 DTO |
| Backend (수정) | `controller/SystemGraphController.java` | `GET /admin/system-graph/api/infra` 엔드포인트 추가 |
| Frontend (수정) | `templates/admin-system-graph.html` | A 탭 본문 교체 (필터 + 상세 패널 + vis-network 로더) |
| DB | **없음** | — |

**신규 2개 + 수정 2개. DB/Entity 변경 없음.**

---

## 9. 리스크 평가

| 리스크 | 수준 | 완화책 |
|--------|------|--------|
| 비밀번호/민감 필드 유출 | **높음** | FR-3, NFR-3 — 화이트리스트 DTO + grep 검증 테스트 |
| N+1 쿼리로 느림 | 중간 | NFR-4 — 레벨별 projection 쿼리 3개(JOIN 또는 별도 SELECT), 메모리에서 조립. 개발계획서에서 SQL 로그로 검증. |
| 초기 데이터 0건 → 빈 화면으로 오류 오인 | 낮음 | FR-12 안내 문구 |
| 데이터 급증 시 응답 용량 초과 | 낮음 | NFR-2 상한 명시, 초과 시 Phase 2 분할 로딩 |
| XSS (운영자가 DB 에 스크립트 삽입) | 낮음 | FR-9 escape 경유 |
| 필터가 서버 쿼리에 반영 안 된 채 클라이언트에서만 숨김 | 낮음 | 현재 규모 OK, NFR-2 모니터링으로 성능 이슈 조기 발견 |

---

## 10. 승인 요청

본 기획서에 대한 codex 검토 및 사용자 최종승인을 요청합니다.
