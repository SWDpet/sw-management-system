-- V014: 최종 합계금액(grand_total) + 낙찰율(bid_rate) 컬럼 추가
-- grand_total: 견적서 출력물에 표시되는 최종 합계 (공급가액+VAT, 절사+낙찰율 적용)
-- bid_rate: 낙찰율 (%, null이면 미적용)

-- 견적서 테이블에 grand_total, bid_rate 추가
ALTER TABLE qt_quotation ADD COLUMN IF NOT EXISTS grand_total BIGINT;
ALTER TABLE qt_quotation ADD COLUMN IF NOT EXISTS bid_rate DOUBLE PRECISION;

-- 대장 테이블에 grand_total 추가
ALTER TABLE qt_quotation_ledger ADD COLUMN IF NOT EXISTS grand_total BIGINT;

-- 기존 데이터: grand_total 계산하여 채우기
-- VAT 미포함(vat_included=false 또는 null): supply = floor(rawTotal/rdUnit)*rdUnit, vat = floor(supply*10/100/rdUnit)*rdUnit, grand = supply + vat
-- VAT 포함(vat_included=true): grand = floor(rawTotal/rdUnit)*rdUnit
-- rawTotal = 품목 합계 (total_amount에 저장된 값)

-- 1) VAT 포함인 견적서
UPDATE qt_quotation SET grand_total =
    CASE WHEN COALESCE(rounddown_unit, 1) <= 1 THEN COALESCE(total_amount, 0)
         ELSE (COALESCE(total_amount, 0) / COALESCE(rounddown_unit, 1)) * COALESCE(rounddown_unit, 1)
    END
WHERE vat_included = true;

-- 2) VAT 미포함인 견적서 (기본값)
UPDATE qt_quotation SET grand_total =
    CASE WHEN COALESCE(rounddown_unit, 1) <= 1 THEN
        COALESCE(total_amount, 0) + COALESCE(total_amount, 0) * 10 / 100
    ELSE
        (COALESCE(total_amount, 0) / COALESCE(rounddown_unit, 1)) * COALESCE(rounddown_unit, 1)
        + ((((COALESCE(total_amount, 0) / COALESCE(rounddown_unit, 1)) * COALESCE(rounddown_unit, 1)) * 10 / 100) / COALESCE(rounddown_unit, 1)) * COALESCE(rounddown_unit, 1)
    END
WHERE vat_included IS NULL OR vat_included = false;

-- 3) 대장 테이블도 동기화
UPDATE qt_quotation_ledger l SET grand_total = q.grand_total
FROM qt_quotation q WHERE l.quote_id = q.quote_id;
