package com.moli.ai.server.bi.constant;

/**
 * ChatBI Shiro 权限码（契约 §1.1）。
 */
public final class BiChatPermissionConstants {

    public static final String QUERY = "ai:chat:query";
    public static final String TRACE = "ai:chat:trace";
    public static final String TRACE_ALL = "ai:chat:trace:all";

    private BiChatPermissionConstants() {
    }
}
