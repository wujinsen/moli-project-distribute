package com.moli.user.center.server.operation.support;

import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 本机运行环境探测（部署脚本仅 Linux 可用）。
 */
@Component
public class OperationHostEnvironment {

    public boolean isLocalLinux() {
        return !System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }
}
