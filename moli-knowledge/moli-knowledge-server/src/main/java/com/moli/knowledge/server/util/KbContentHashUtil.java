package com.moli.knowledge.server.util;

import com.moli.common.exception.BaseException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** 与 {@code sync_to_db.py} 一致：全文 UTF-8 SHA-256。 */
public final class KbContentHashUtil {

    private KbContentHashUtil() {
    }

    public static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new BaseException("计算 hash 失败：" + e.getMessage());
        }
    }
}
