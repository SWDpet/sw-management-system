package com.swmanager.system.dto.support;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * 외부 의존성 없는 ULID(26자 Crockford Base32) 생성기.
 * (sprint team-monitor-dashboard, 개발계획 Step 1-3)
 *
 * - 앞 10자: timestamp (Instant.now().toEpochMilli, 48bit).
 * - 뒤 16자: SecureRandom 80bit.
 * - 모노토닉 보장은 비범위 (단일 노드 + 클라이언트 dedupe 목적이라 충분).
 */
public final class UlidGenerator {

    private static final char[] ENCODING = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private UlidGenerator() {}

    public static String next() {
        long ts = Instant.now().toEpochMilli();
        byte[] rand = new byte[10];
        RANDOM.nextBytes(rand);

        char[] out = new char[26];
        // timestamp (48bit) → 10자
        for (int i = 9; i >= 0; i--) {
            out[i] = ENCODING[(int) (ts & 0x1F)];
            ts >>>= 5;
        }
        // randomness (80bit) → 16자
        long hi = ((rand[0] & 0xFFL) << 32)
                | ((rand[1] & 0xFFL) << 24)
                | ((rand[2] & 0xFFL) << 16)
                | ((rand[3] & 0xFFL) << 8)
                |  (rand[4] & 0xFFL);
        long lo = ((rand[5] & 0xFFL) << 32)
                | ((rand[6] & 0xFFL) << 24)
                | ((rand[7] & 0xFFL) << 16)
                | ((rand[8] & 0xFFL) << 8)
                |  (rand[9] & 0xFFL);
        for (int i = 17; i >= 10; i--) {
            out[i] = ENCODING[(int) (hi & 0x1F)];
            hi >>>= 5;
        }
        for (int i = 25; i >= 18; i--) {
            out[i] = ENCODING[(int) (lo & 0x1F)];
            lo >>>= 5;
        }
        return new String(out);
    }
}
