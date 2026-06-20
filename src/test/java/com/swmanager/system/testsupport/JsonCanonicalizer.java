package com.swmanager.system.testsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * JSON 정규화기 (net 인프라 #2, QUALITY_CHARTER §4-1).
 *
 * golden 스냅샷이 <b>키 순서</b>나 <b>휘발값</b>(DB 생성 ID·타임스탬프 등) 때문에 깨지지 않도록
 * 응답 JSON 을 결정적 형태로 정규화한다:
 * <ul>
 *   <li>모든 객체 키를 재귀적으로 <b>알파벳 정렬</b> (키 순서 비교 금지 — codex 권고)</li>
 *   <li>지정한 <b>휘발 키</b>의 값을 {@code "<volatile>"} 로 치환 (키·구조는 보존, 값만 정규화)</li>
 * </ul>
 *
 * 사용: {@code JsonCanonicalizer.canonicalize(json)} 또는
 *       {@code JsonCanonicalizer.canonicalize(json, Set.of("createdAt","staff_id"))}.
 */
public final class JsonCanonicalizer {

    private JsonCanonicalizer() {}

    /** 키 정렬 직렬화기 (Map 키를 알파벳 정렬). */
    private static final ObjectMapper SORTED = JsonMapper.builder()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .build();

    /** 휘발 키 없이 키만 정렬. */
    public static String canonicalize(String json) {
        return canonicalize(json, Set.of());
    }

    /**
     * 키 정렬 + 휘발 키 값 마스킹.
     * @param json         정규화할 JSON 문자열
     * @param volatileKeys 값을 {@code "<volatile>"} 로 대체할 키 이름들 (대소문자 무시)
     */
    public static String canonicalize(String json, Set<String> volatileKeys) {
        try {
            Object tree = SORTED.readValue(json, Object.class); // Map/List/스칼라
            Set<String> lower = lower(volatileKeys);
            Object masked = mask(tree, lower);
            return SORTED.writeValueAsString(masked);
        } catch (Exception e) {
            throw new IllegalArgumentException("정규화 실패: " + e.getMessage() + " / json=" + preview(json), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object mask(Object node, Set<String> volatileLower) {
        if (node instanceof Map<?, ?> m) {
            // TreeMap 으로 키 정렬 보강(직렬화기와 이중 안전) + 휘발 마스킹
            Map<String, Object> out = new TreeMap<>();
            for (Map.Entry<?, ?> e : m.entrySet()) {
                String key = String.valueOf(e.getKey());
                if (volatileLower.contains(key.toLowerCase())) {
                    out.put(key, "<volatile>");
                } else {
                    out.put(key, mask(e.getValue(), volatileLower));
                }
            }
            return out;
        }
        if (node instanceof List<?> list) {
            return list.stream().map(v -> mask(v, volatileLower)).toList();
        }
        return node; // 스칼라 그대로
    }

    private static Set<String> lower(Set<String> keys) {
        return keys.stream().map(String::toLowerCase).collect(java.util.stream.Collectors.toSet());
    }

    private static String preview(String s) {
        if (s == null) return "null";
        return s.length() > 120 ? s.substring(0, 120) + "…" : s;
    }
}
