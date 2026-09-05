package com.moli.common.core;

import org.slf4j.MDC;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从 SkyWalking Toolkit / MDC 取出<strong>根</strong> Trace ID（32 位 hex）。
 * <p>
 * 不依赖 skywalking-toolkit：common 不能绑 Agent 版本。有 Toolkit 时反射调用
 * {@code TraceContext.traceId()}；没有则读 MDC。UI 复制的
 * {@code TID:hex.segment.xxx} 一律收成前 32 位，方便贴 Loki / Grafana。
 * 信封里只回 {@code traceId}，不回 {@code spanId}。
 */
public final class TraceIds {

    private static final Pattern HEX32 = Pattern.compile("(?i)(?:TID:)?([0-9a-f]{32})");
    private static final String[] MDC_KEYS = {"tid", "TID", "trace_id", "traceId", "SW_CTX"};

    private TraceIds() {
    }

    public static String currentRoot() {
        return rootId(currentRaw());
    }

    public static String currentRaw() {
        String fromToolkit = fromToolkit();
        if (usable(fromToolkit)) {
            return fromToolkit.trim();
        }
        for (String key : MDC_KEYS) {
            String value = MDC.get(key);
            if (usable(value)) {
                return value.trim();
            }
        }
        return null;
    }

    /**
     * 抽出 32 位根 ID。无法识别时返回 {@code null}，不抛。
     */
    public static String rootId(String raw) {
        if (!usable(raw)) {
            return null;
        }
        Matcher matcher = HEX32.matcher(raw.trim());
        if (matcher.find()) {
            return matcher.group(1).toLowerCase(Locale.ROOT);
        }
        return null;
    }

    private static boolean usable(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        return !trimmed.isEmpty()
                && !"N/A".equalsIgnoreCase(trimmed)
                && !"Ignored_Trace".equalsIgnoreCase(trimmed);
    }

    private static String fromToolkit() {
        try {
            Class<?> type = Class.forName("org.apache.skywalking.apm.toolkit.trace.TraceContext");
            Object id = type.getMethod("traceId").invoke(null);
            return id == null ? null : String.valueOf(id);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
