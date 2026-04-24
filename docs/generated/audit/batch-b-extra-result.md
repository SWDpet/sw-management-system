# 배치 B 추가 검증

## qt_wage_rate 컬럼

| column_name | data_type |
|---|---|
| wage_id | bigint |
| year | integer |
| grade_name | character varying |
| daily_rate | bigint |
| monthly_rate | bigint |
| hourly_rate | bigint |
| description | character varying |

총 7행

## qt_wage_rate 샘플 5건

| wage_id | year | grade_name | daily_rate | monthly_rate | hourly_rate | description |
|---|---|---|---|---|---|---|
| 18 | 2026 | 기술사 | 442679 | 0 | 0 | 측량업체-기술계 |
| 19 | 2026 | 특급기술자 | 326774 | 0 | 0 | 측량업체-기술계 |
| 20 | 2026 | 고급기술자 | 289265 | 0 | 0 | 측량업체-기술계 |
| 21 | 2026 | 중급기술자 | 251596 | 0 | 0 | 측량업체-기술계 |
| 22 | 2026 | 초급기술자 | 215609 | 0 | 0 | 측량업체-기술계 |

총 5행

## qt_product_pattern 샘플

❌ 오류: "pattern_name" 이름의 칼럼은 없습니다
  Position: 30

## qt_remarks_pattern 샘플

| pattern_id | pattern_name | content | sort_order |
|---|---|---|---|
| 1 | 담당자 박욱진 | ※ 담당자 : SW지원부 박욱진 이사 (T.070-7113-8093  M.010-3056-2678  F.02-561-9792) | 0 |
| 2 | 담당자 이동수 | ※ 담당자 : GIS사업부 이동수 이사 (T.070-7113-9894  M.010-9755-1316  F.053-817-9987  E-mail : leeds@uitgis.com) | 0 |
| 3 | 담당자 여현정 | ※ 담당자 : SW영업본부 여현정 이사 (T.070-7113-8072  M.010-2815-0957  F.02-561-9792) | 0 |
| 4 | 유효기간 30일, 담당자 박욱진 | 1. 본 견적의 유효기간은 발급일로부터 30일 입니다.  ※ 담당자 : SW지원부 박욱진 이사 (T.070-7113-8093  M.010-3056-2678  F.02-561-9792) | 0 |
| 5 | 무상+담당자 박욱진 | ※ 무상하자보수 기간 : 구매일로부터 1년 ※ 담당자 : SW지원부 박욱진 이사 (T.070-7113-8093  M.010-3056-2678  F.02-561-9792) | 0 |
| 6 | 공급가 10%+담당자 박욱진 | ※ 상기 견적은 제품 공급가에 10% 요율을 적용한 금액입니다. ※ 담당자 : SW지원부 박욱진 이사 (T.070-7113-8093  M.010-3056-2678  F.02-561-9792) | 0 |
| 7 | 조달제품+공급가16%+담당자 박욱진 | ※ 상기 제품은 조달청 디지털몰에 등록되어 있습니다. ※ 상기 견적은 제품 공급가에 16% 요율을 적용한 금액입니다. ※ 담당자 : SW지원부 박욱진 이사 (T.070-7113-8093  M.010-3056-2678  F.02-561-9792) | 0 |

총 7행

## qt_product_pattern 컬럼

| column_name | data_type |
|---|---|
| pattern_id | bigint |
| category | character varying |
| pattern_group | character varying |
| product_name | character varying |
| default_unit | character varying |
| default_unit_price | bigint |
| description | character varying |
| sub_items | text |
| usage_count | integer |
| calc_type | character varying |
| overhead_rate | double precision |
| tech_fee_rate | double precision |

총 12행

## qt_remarks_pattern 컬럼

| column_name | data_type |
|---|---|
| pattern_id | bigint |
| pattern_name | character varying |
| content | text |
| sort_order | integer |

총 4행

## license_registry.country 오염 추정 값 — 사람 이름/이상치

| country | full_name | company | cnt |
|---|---|---|---|
| kim hanjun | 공원녹지관리시스템 개발서버(이용탁) | (주)정도유아이티 | 1 |
| kim hanjun | 김나윤 | (주)정도유아이티 | 1 |
| kim hanjun | 김수빈 | 광명시청 | 1 |
| kim hanjun | 라오스 도시계획 정보 플랫폼 GIS SW Server2(이석호) | (주)정도유아이티 | 1 |
| kim hanjun | 허치영(Dekstop) | (주)정도유아이티 | 1 |
| Park Uk Jin | Daon LAB Free License | DaonSoft | 1 |
| seo hyeon gyu | 경찰청(공통서버 #1) | 경찰청 | 1 |
| seo hyeon gyu | 경찰청(공통서버 #2) | 경찰청 | 1 |
| seo hyeon gyu | 경찰청(공통서버 #3) | 경찰청 | 1 |
| seo hyeon gyu | 경찰청(공통서버 #4) | 경찰청 | 1 |
| seo hyeon gyu | 장근주 | 영월군청 | 1 |
| seo hyeon gyu | 황원모 | 안동시청 | 1 |
| 김한준 | 진완수 | 양양군청-도시계획과 | 1 |
| 김한준 | 최형인 | 양양군청-도시계획과 | 1 |
| 서현규 | SW지원부_193번 테스트서버 | (주)정도유아이티 | 1 |
| 서현규 | 고령군 기초조사 운영서버 | 고령군청 | 1 |
| 서현규 | 김웅중 | 울산광역시청-정보통신과(테스트) | 1 |
| 서현규 | 김웅중 | 울산광역시청-정보통신과(테스트2) | 1 |
| 서현규 | 도해경 | 조달청 | 1 |
| 서현규 | 도해경(개발서버) | 조달청 | 1 |

총 20행

