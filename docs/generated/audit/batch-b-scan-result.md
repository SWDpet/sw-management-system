# 배치 B 실데이터 분포 — 견적서·라이선스

## qt_quotation 레코드 수 + 상태·분류 분포

| status | category | cnt |
|---|---|---|
| 발행완료 | 유지보수 | 42 |
| 발행완료 | 용역 | 12 |
| 발행완료 | 제품 | 5 |

총 3행

## qt_quotation.category distinct

| category | cnt |
|---|---|
| 유지보수 | 42 |
| 용역 | 12 |
| 제품 | 5 |

총 3행

## qt_quotation.status distinct

| status | cnt |
|---|---|
| 발행완료 | 59 |

총 1행

## qt_quotation.template_type distinct

| template_type | cnt |
|---|---|
| 1 | 56 |
| 2 | 3 |

총 2행

## qt_quotation_item 레코드 수 + 단위 distinct

| unit | cnt |
|---|---|
| 식 | 85 |

총 1행

## qt_product_pattern 레코드 수

| total |
|---|
| 23 |

총 1행

## qt_product_pattern.category distinct

| category | cnt |
|---|---|
| 용역 | 10 |
| 제품 | 7 |
| 유지보수 | 6 |

총 3행

## qt_wage_rate 레코드 수 + 등급 distinct

❌ 오류: "tech_grade" 이름의 칼럼은 없습니다
  Position: 8

## qt_remarks_pattern 레코드 수

| total |
|---|
| 7 |

총 1행

## qt_quotation_ledger 레코드 수

| total |
|---|
| 59 |

총 1행

## qt_quote_number_seq

| seq_id | year | category | last_seq |
|---|---|---|---|
| 2 | 2026 | 제품 | 5 |
| 3 | 2026 | 유지보수 | 42 |
| 1 | 2026 | 용역 | 15 |

총 3행

## qt_quotation 컬럼 조회

| column_name | data_type |
|---|---|
| quote_id | bigint |
| quote_number | character varying |
| quote_date | date |
| category | character varying |
| project_name | character varying |
| recipient | character varying |
| reference_to | character varying |
| total_amount | bigint |
| total_amount_text | character varying |
| vat_included | boolean |
| status | character varying |
| created_by | character varying |
| created_at | timestamp without time zone |
| updated_at | timestamp without time zone |
| rounddown_unit | integer |
| show_seal | boolean |
| template_type | integer |
| remarks | character varying |
| grand_total | bigint |
| bid_rate | double precision |

총 20행

## license_registry 레코드 수

| total |
|---|
| 1531 |

총 1행

## license_registry.license_type distinct

| license_type | cnt |
|---|---|
| License Text | 1509 |
| Floating License Text | 22 |

총 2행

## license_registry.generation_source distinct

| generation_source | cnt |
|---|---|
| Manual | 1531 |

총 1행

## license_registry.country distinct

| country | cnt |
|---|---|
| Korea | 649 |
|  | 600 |
| korea | 108 |
| null | 93 |
| 서현규 | 18 |
| Vietnam | 12 |
| vietnam | 9 |
| seo hyeon gyu | 6 |
| kim hanjun | 5 |
| Seoul | 4 |
| KOREA | 4 |
| 김한준 | 2 |
| Cheonan | 2 |
| Anyang-si | 2 |
| Kroea | 2 |
| Gyeongsangbuk-do | 2 |
| Yangyang-gun | 2 |
| Gyeonggi-do | 2 |
| jeju | 1 |
| Park Uk Jin | 1 |

총 20행

## license_registry.valid_product_edition distinct

| valid_product_edition | cnt |
|---|---|
| null | 1529 |
| 1f981d496-3f72-37f3-b2bf-dbae850f3ae1&&237933c32-d3d5-3779-9cf8-3467b763178a | 1 |
| 2cbf40ca8-9d21-3bf6-a750-efe8604a59b2&&14fdffb30-49d2-379d-89c8-f4863978650b | 1 |

총 3행

## license_registry.product_id distinct

| product_id | cnt |
|---|---|
| SDK Professional 1.0 for LSA | 695 |
| Maple Basic & SDK 1.x | 221 |
| Maple Pro 1.x | 212 |
| Maple Pro 4.0 | 119 |
| GeoNURIS MapStudio | 114 |
| MapStudio 4.0 | 70 |
| LSA Basic 2.5 | 42 |
| Maple Basic & SDK 4.0 | 17 |
| LSA Pro 2.5 | 15 |
| LSA Super 2.5 | 10 |
| LSA Slim Pro 3.0 | 4 |
| LSA Pro 3.0 | 3 |
| LSA Slim Basic 3.0 | 3 |
| LSA Super 3.0 | 3 |
| LSA Basic 3.0 | 3 |

총 15행

## license_upload_history 레코드 수

| total |
|---|
| 29 |

총 1행

## geonuris_license 레코드 수

| total |
|---|
| 16 |

총 1행

## geonuris_license.license_type distinct

| license_type | cnt |
|---|---|
| DESKTOP | 7 |
| GSS30 | 6 |
| GSS35 | 3 |

총 3행

## geonuris_license 컬럼

| column_name | data_type |
|---|---|
| id | bigint |
| license_type | character varying |
| file_name | character varying |
| user_name | character varying |
| organization | character varying |
| phone | character varying |
| email | character varying |
| issuer | character varying |
| mac_address | character varying |
| permission | character varying |
| start_date | timestamp without time zone |
| expiry_date | timestamp without time zone |
| dbms_type | character varying |
| setl_count | integer |
| plugin_edit | boolean |
| plugin_gdm | boolean |
| plugin_tmbuilder | boolean |
| plugin_setl | boolean |
| license_data | text |
| created_at | timestamp without time zone |
| created_by | character varying |

총 21행

## qt_quotation.category vs prj_types 매칭

| value | cnt | match |
|---|---|---|
| 유지보수 | 42 | ❌ |
| 용역 | 12 | ❌ |
| 제품 | 5 | ❌ |

총 3행

## qt_wage_rate.tech_grade 샘플

❌ 오류: "tech_grade" 이름의 칼럼은 없습니다
  Position: 8

