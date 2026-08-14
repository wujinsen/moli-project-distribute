package com.moli.user.center.server.operation.ssh;

import com.jcraft.jsch.Session;

import java.io.Closeable;

/**
 * 已连接的 SSH 会话包装，记录实际连接的主机，便于 try-with-resources 自动断开。
 */
public class OperationSshSession implements Closeable {

    private final Session session;
    private final String host;

    public OperationSshSession(Session session, String host) {
        this.session = session;
        this.host = host;
    }

    public Session getSession() {
        return session;
    }

    public String getHost() {
        return host;
    }

    @Override
    public void close() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}
