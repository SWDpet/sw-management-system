# 색 토큰 통일 매핑 (레거시 → DESIGN.md 토큰)

> 목적: 페이지별로 흩어진 SB-Admin·Material 잔재 색을 **DESIGN.md SoT 토큰**으로 통일.
> 적용: `static/css/design-system.css` 의 `var(--*)` 로 치환. 점진 마이그레이션(신규/수정 화면 우선).
> SoT: `docs/DESIGN.md` · 구현: `static/css/design-system.css`

## 통일 팔레트 (DESIGN.md)
| 의미 | 토큰 | HEX |
|---|---|---|
| 브랜드 | `--primary` | `#0F766E` |
| 브랜드 진하게 | `--primary2` | `#115E59` |
| 완료/성공 | `--success` | `#059669` |
| 진행/주의 | `--warning` | `#D97706` |
| 삭제/에러 | `--danger` | `#DC2626` |
| 정보 | `--info` | `#0891B2` |
| 보조 텍스트 | `--text2` | `#57534E` |
| 힌트/라벨 | `--muted` | `#A8A29E` |

## 레거시색 → 토큰 (치환 가이드)
| 레거시 HEX | 출처 | 빈도 | → 치환 토큰 | 비고 |
|---|---|---|---|---|
| `#134E4A` | teal 변형 | 62 | `--primary2` (#115E59) | 거의 동일, 통일 |
| `#1CC88A` | SB-Admin 녹 | 53 | `--success` (#059669) | "성공/완료" 통일 |
| `#2E7D32` | Material 녹 | 23 | `--success` | 〃 |
| `#858796` | SB-Admin 회 | 50 | `--text2`/`--muted` | 텍스트=text2, 라벨/보더=muted |
| `#F6C23E` | SB-Admin 황 | 22 | `--warning` (#D97706) | "진행/주의" 통일 |
| `#E64A19` | Material 주황 | 9 | `--warning` | 진행중 강조 → warning |
| `#36B9CC` | SB-Admin 청 | 9 | `--info` (#0891B2) | "정보" 통일 |
| `#4E73DF` | SB-Admin 파 | 1 | `--primary` | 일반 액센트 → 브랜드 teal |
| `#7B1FA2` | Material 보라 | 12 | `--primary` / `--primary-lt` | 시스템에 보라 없음 → teal 로 흡수 |

## 상태 뱃지 색 (DESIGN.md)
| 상태 | 배경 | 텍스트 | 보더 |
|---|---|---|---|
| 완료(success) | `#F0FDF4` | `#166534` | `#BBF7D0` |
| 진행(warning) | `#FFFBEB` | `#92400E` | `#FDE68A` |
| 만료(danger) | `#FEF2F2` | `#B91C1C` | `#FECACA` |
| 정보(info) | `#ECFEFF` | `#155E75` | `#A5F3FC` |

## 적용 순서 (점진)
1. `design-system.css` 전역 link (토큰 var 제공) — 시각 변화 거의 없음(안전)
2. 신규/수정하는 화면부터 인라인 색 → `var(--*)` 치환 + 컴포넌트 클래스(`ds-*`) 채택
3. 사용 빈도 높은 페이지(대시보드·목록·점검내역서) 우선 정리
4. 레거시 HEX 0 될 때까지 grep 추적 (`#1cc88a|#f6c23e|#36b9cc|#7b1fa2|#2e7d32|#e64a19|#858796|#4e73df`)
