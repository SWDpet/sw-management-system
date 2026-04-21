package com.swmanager.system.constant.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * access_logs.action_type Enum (S9 access-log-action-and-menu-sync).
 *
 * 정책:
 *  - DB 저장값은 한글 label (기존 access_logs.action_type VARCHAR 데이터 호환)
 *  - label Freeze (기획서 NFR-7): 본 상수의 한글 label 은 불변.
 *    변경 시 반드시 별도 마이그레이션 스프린트 선행.
 *  - 유효값 개수: 정확히 13 (FR-1).
 *
 * JSON 바인딩:
 *  - 직렬화: label 을 사용 (@JsonValue)
 *  - 역직렬화: label 우선, 실패 시 Enum name, 둘 다 trim 적용
 */
public enum AccessActionType {
    VIEW("조회"),
    CREATE("등록"),
    UPDATE("수정"),
    DELETE("삭제"),
    DOWNLOAD("다운로드"),
    UPLOAD("업로드"),
    APPROVE("승인"),
    SIGN("서명"),
    PREVIEW("미리보기"),
    BATCH("일괄처리"),
    PATTERN_CRUD("패턴관리"),
    WAGE_CRUD("노임관리"),
    SENSITIVE_VIEW("민감정보조회");

    private final String label;

    /**
     * 동의어(기존 리터럴) → Enum 매핑.
     * 기획서 §4-1 "기존 리터럴 통합 후보" 열과 1:1 일치.
     * 신규 리터럴 출현 시 본 맵에 추가하거나 Enum 상수를 신설 (후속 스프린트).
     */
    private static final Map<String, AccessActionType> SYNONYMS = new ConcurrentHashMap<>();
    static {
        // VIEW
        put("목록조회", VIEW);
        put("상세조회", VIEW);
        put("접근", VIEW);
        put("발급폼접근", VIEW);
        put("수정폼접근", VIEW);
        // CREATE
        put("생성", CREATE);
        put("발급", CREATE);
        put("신청", CREATE);
        // UPDATE
        put("정보수정", UPDATE);
        put("비번변경", UPDATE);
        put("상태변경", UPDATE);
        // DOWNLOAD
        put("CSV다운로드", DOWNLOAD);
        // APPROVE
        put("승인요청", APPROVE);
        // BATCH
        put("일괄생성", BATCH);
        put("집계", BATCH);
        put("금액재계산", BATCH);
        put("패턴복사", BATCH);
        put("패턴초기화", BATCH);
        // PATTERN_CRUD
        put("패턴등록", PATTERN_CRUD);
        put("패턴수정", PATTERN_CRUD);
        put("패턴삭제", PATTERN_CRUD);
        put("비고패턴등록", PATTERN_CRUD);
        put("비고패턴수정", PATTERN_CRUD);
        put("비고패턴삭제", PATTERN_CRUD);
        // WAGE_CRUD
        put("노임단가등록", WAGE_CRUD);
        put("노임단가수정", WAGE_CRUD);
        put("노임단가삭제", WAGE_CRUD);
    }

    private static void put(String synonym, AccessActionType v) {
        SYNONYMS.put(synonym, v);
    }

    AccessActionType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    /**
     * JSON 역직렬화 진입점.
     * label(한글) 우선 매칭 → Enum name 매칭 순. 둘 다 trim 적용.
     */
    @JsonCreator
    public static AccessActionType fromJson(String raw) {
        if (raw == null) return null;
        String norm = raw.trim();
        AccessActionType v = fromKoLabel(norm);
        if (v != null) return v;
        try {
            return AccessActionType.valueOf(norm);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 한글 label + 동의어 정규화 (기획서 §4-1-A).
     *
     * 1) trim
     * 2) 공식 label 정확 매칭
     * 3) 동의어 맵 조회
     * 4) 매칭 실패 시 null (호출측 fail-soft 처리)
     */
    public static AccessActionType fromKoLabel(String raw) {
        if (raw == null) return null;
        String norm = raw.trim();
        for (AccessActionType v : values()) {
            if (v.label.equals(norm)) return v;
        }
        return SYNONYMS.get(norm);
    }
}
