package com.moli.user.center.server.operation.health;

import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * TCP 端口探活（短超时，避免阻塞请求线程过久）。
 */
public final class OperationTcpProbe {

    private static final int DEFAULT_TIMEOUT_MS = 3_000;

    private OperationTcpProbe() {
    }

    public static int probe(String host, String portText) {
        if (StringUtils.isBlank(host)) {
            return OperationHealthStatus.SKIPPED;
        }
        Integer port = parsePort(portText);
        if (port == null) {
            return OperationHealthStatus.SKIPPED;
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host.trim(), port), DEFAULT_TIMEOUT_MS);
            return OperationHealthStatus.UP;
        } catch (Exception e) {
            return OperationHealthStatus.DOWN;
        }
    }

    static Integer parsePort(String portText) {
        if (StringUtils.isBlank(portText) || "-".equals(portText.trim())) {
            return null;
        }
        String digits = portText.trim().replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return null;
        }
        try {
            int port = Integer.parseInt(digits);
            return port > 0 && port <= 65535 ? port : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
