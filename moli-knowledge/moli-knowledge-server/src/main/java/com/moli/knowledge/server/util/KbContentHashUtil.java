package com.moli.knowledge.server.util;

import com.moli.common.exception.BaseException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** 与 {@code sync_to_db.py} 一致：全文 UTF-8 SHA-256。 */
public final class KbContentHashUtil {

    private KbContentHashUtil() {
    }

    /**
     * 与 Python {@code Path.read_text(encoding="utf-8")} 一致：Universal newlines 归一为 LF 后再 hash。
     * Windows CRLF wiki 若直接 hash 原始字节会与 DB（sync 口径）全员不一致。
     */
    public static String sha256WikiMarkdown(String rawUtf8) {
        return sha256(normalizeWikiNewlines(rawUtf8));
    }

    public static String normalizeWikiNewlines(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\r\n", "\n").replace("\r", "\n");
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
