package com.swmanager.system.dto.inspection;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * /api/inspection/qr-batch 요청 바디.
 *
 * PWA decoder.mjs 가 PoC frames 를 디코드해 얻은 payload + header 를 그대로 전송한다.
 * 기획서: docs/product-specs/inspection-qr-batch.md §3 FR-2
 */
@Getter
@Setter
public class InspectionQrBatchRequest {

    @Valid
    @NotNull
    private Payload payload;

    @Valid
    @NotNull
    private Header header;

    @Getter
    @Setter
    public static class Payload {

        /** snapshot tag (예: "snapshot/qr1") — 의미 없음, 호환용. */
        private String s;

        /** 멱등 키 (예: "46810_UPIS-2026-05-ap"). site_code 신규 규칙({adm}_{sys_nm_en})상 대문자 허용. */
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_\\-:]{1,64}$", message = "id must match ^[A-Za-z0-9_\\-:]{1,64}$")
        private String id;

        // site_code 신규 규칙: {adm_sect_c}_{sys_nm_en} (예: 46810_UPIS). 시스템 영문은 대문자.
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_\\-]{1,32}$", message = "site must match ^[A-Za-z0-9_\\-]{1,32}$")
        private String site;

        @NotBlank
        @Pattern(regexp = "^[0-9]{4}-[0-9]{2}$", message = "round must match ^[0-9]{4}-[0-9]{2}$")
        private String round;

        /** unix seconds (점검 수행 시각). */
        private Long ts;

        /** 점검자 이름 (PoC payload 보존용 — UI 검토 후 user 매핑은 후속). */
        private String inspector;

        /** tier key (ap/db/gis/…) → Tier. */
        @NotEmpty
        private Map<String, Tier> tiers;
    }

    @Getter
    @Setter
    public static class Tier {

        /** hostname (예: "UPIS-AP"). */
        private String h;

        /** OS (예: "WinSvr 2012R2"). */
        private String os;

        /**
         * [key, status, value] 트리플 배열.
         *   key:    metric key (예: "ap.perf.cpu_pct")
         *   status: "ok" | "warn" | "err" | "M"
         *   value:  number | string | bool | null
         */
        @JsonProperty("i")
        @NotNull
        private List<List<Object>> items;
    }

    @Getter
    @Setter
    public static class Header {

        /** sha1 hex[:6] of gzipped payload — 보조 검증용 (NFR-3 warn-only). */
        @NotBlank
        private String hash;

        @JsonProperty("raw_bytes")
        private Integer rawBytes;

        @JsonProperty("gz_bytes")
        private Integer gzBytes;

        @JsonProperty("b45_chars")
        private Integer b45Chars;

        private Integer total;
    }
}
