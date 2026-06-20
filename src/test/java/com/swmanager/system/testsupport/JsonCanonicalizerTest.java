package com.swmanager.system.testsupport;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JsonCanonicalizer 단위 테스트 (net 인프라 검증).
 */
class JsonCanonicalizerTest {

    @Test
    void sortsObjectKeys_recursively() {
        String json = "{\"b\":1,\"a\":2,\"c\":{\"y\":1,\"x\":2}}";
        assertThat(JsonCanonicalizer.canonicalize(json))
                .isEqualTo("{\"a\":2,\"b\":1,\"c\":{\"x\":2,\"y\":1}}");
    }

    @Test
    void differentKeyOrder_yieldsIdenticalCanonical() {
        // golden 안정성의 핵심: 키 순서가 달라도 같은 정규형
        String a = "{\"success\":true,\"data\":{\"name\":\"x\",\"id\":1}}";
        String b = "{\"data\":{\"id\":1,\"name\":\"x\"},\"success\":true}";
        assertThat(JsonCanonicalizer.canonicalize(a))
                .isEqualTo(JsonCanonicalizer.canonicalize(b));
    }

    @Test
    void masksVolatileKeys_keepingStructure() {
        String json = "{\"staff_id\":99,\"name\":\"홍길동\",\"createdAt\":\"2026-06-20T10:00:00\"}";
        String out = JsonCanonicalizer.canonicalize(json, Set.of("staff_id", "createdAt"));
        // 키·구조는 보존, 휘발값만 <volatile>
        assertThat(out).isEqualTo(
                "{\"createdAt\":\"<volatile>\",\"name\":\"홍길동\",\"staff_id\":\"<volatile>\"}");
    }

    @Test
    void volatileKey_caseInsensitive() {
        String json = "{\"StaffId\":99,\"name\":\"x\"}";
        assertThat(JsonCanonicalizer.canonicalize(json, Set.of("staffid")))
                .contains("\"StaffId\":\"<volatile>\"");
    }

    @Test
    void masksVolatileInsideArraysAndNested() {
        String json = "{\"items\":[{\"id\":1,\"v\":\"a\"},{\"id\":2,\"v\":\"b\"}]}";
        String out = JsonCanonicalizer.canonicalize(json, Set.of("id"));
        assertThat(out).isEqualTo(
                "{\"items\":[{\"id\":\"<volatile>\",\"v\":\"a\"},{\"id\":\"<volatile>\",\"v\":\"b\"}]}");
    }

    @Test
    void idempotent() {
        String json = "{\"b\":[3,1,2],\"a\":{\"z\":1,\"y\":2}}";
        String once = JsonCanonicalizer.canonicalize(json);
        assertThat(JsonCanonicalizer.canonicalize(once)).isEqualTo(once);
        // 배열 순서는 보존(정렬 안 함 — 의미 있는 순서일 수 있음)
        assertThat(once).contains("[3,1,2]");
    }

    @Test
    void handlesTopLevelArrayAndScalars() {
        assertThat(JsonCanonicalizer.canonicalize("[{\"b\":1,\"a\":2}]"))
                .isEqualTo("[{\"a\":2,\"b\":1}]");
        assertThat(JsonCanonicalizer.canonicalize("true")).isEqualTo("true");
    }
}
