# Pre-flight SQL 실행 결과 (스프린트 #1 착수 전)

- 실행일: Mon Apr 20 10:08:02 KST 2026
- DB: jdbc:postgresql://211.104.137.55:5881/SW_Dept

## 1-1. tb_document.status 분포

```sql
SELECT status, COUNT(*) FROM tb_document GROUP BY status ORDER BY COUNT(*) DESC
```

| status | count |
|---|---|
| COMPLETED | 12 |
| DRAFT | 3 |

총 2행

## 1-2. tb_document.doc_type 분포

```sql
SELECT doc_type, COUNT(*) FROM tb_document GROUP BY doc_type ORDER BY COUNT(*) DESC
```

| doc_type | count |
|---|---|
| INSPECT | 8 |
| COMMENCE | 3 |
| COMPLETION | 2 |
| INTERIM | 2 |

총 4행

## 1-3. sw_pjt.sys_nm_en 분포

```sql
SELECT sys_nm_en, COUNT(*) FROM sw_pjt GROUP BY sys_nm_en ORDER BY COUNT(*) DESC
```

| sys_nm_en | count |
|---|---|
| UPIS | 297 |
| KRAS | 279 |
| SC | 5 |
| IPSS | 4 |
| LTCS | 3 |
| 112 | 3 |
| MPMS | 3 |
| APIMS | 2 |

총 8행

## 1-4. inspect_check_result.section 분포

```sql
SELECT section, COUNT(*) FROM inspect_check_result GROUP BY section ORDER BY COUNT(*) DESC
```

| section | count |
|---|---|
| DB | 144 |
| DBMS | 102 |
| APP | 98 |
| AP | 84 |
| GIS | 51 |
| DB_USAGE | 7 |
| AP_USAGE | 4 |
| DBMS_ETC | 2 |
| APP_ETC | 1 |

총 9행

## 1-5. inspect_check_result.result 분포

```sql
SELECT result, COUNT(*) FROM inspect_check_result GROUP BY result ORDER BY COUNT(*) DESC
```

| result | count |
|---|---|
| 정상 | 313 |
|  | 165 |
| 1% | 3 |
| test | 2 |
| 70% | 1 |
| 21% | 1 |
| 87% | 1 |
| 특이사항 없음 | 1 |
| 159GB / 271GB 가용 | 1 |
| 점검 | 1 |
| 21.4GB / 135GB 가용 | 1 |
| 55% | 1 |
| 32% | 1 |
| 48% | 1 |

총 14행

## 1-6. inspect_report.status 분포

```sql
SELECT status, COUNT(*) FROM inspect_report GROUP BY status ORDER BY COUNT(*) DESC
```

| status | count |
|---|---|
| COMPLETED | 7 |
| DRAFT | 3 |

총 2행

## 2-1. sw_pjt.sys_nm_en 공백/대소문자 진단

```sql
SELECT sys_nm_en, TRIM(sys_nm_en) AS trimmed, LOWER(sys_nm_en) AS lowered, COUNT(*) FROM sw_pjt GROUP BY sys_nm_en, TRIM(sys_nm_en), LOWER(sys_nm_en) ORDER BY COUNT(*) DESC
```

| sys_nm_en | trimmed | lowered | count |
|---|---|---|---|
| UPIS | UPIS | upis | 297 |
| KRAS | KRAS | kras | 279 |
| SC | SC | sc | 5 |
| IPSS | IPSS | ipss | 4 |
| MPMS | MPMS | mpms | 3 |
| LTCS | LTCS | ltcs | 3 |
| 112 | 112 | 112 | 3 |
| APIMS | APIMS | apims | 2 |

총 8행

## 2-2. sw_pjt.sys_nm_en NULL/빈값

```sql
SELECT COUNT(*) AS null_or_empty FROM sw_pjt WHERE sys_nm_en IS NULL OR sys_nm_en = ''
```

| null_or_empty |
|---|
| 0 |

총 1행

## 2-3. tb_document.status NULL/빈값

```sql
SELECT COUNT(*) AS null_or_empty FROM tb_document WHERE status IS NULL OR status = ''
```

| null_or_empty |
|---|
| 0 |

총 1행

## 2-4. tb_document.doc_type NULL/빈값

```sql
SELECT COUNT(*) AS null_or_empty FROM tb_document WHERE doc_type IS NULL OR doc_type = ''
```

| null_or_empty |
|---|
| 0 |

총 1행

---

**평가 기준**:
- 값이 Enum 후보와 1:1 정렬되면 `@Enumerated(STRING)` 가능
- 대소문자/공백 불일치만 있으면 `AttributeConverter` 정규화로 해결
- 별칭/레거시 값(한글 등)이 있으면 **Pre-flight 데이터 정제 스크립트** 선행 필요
