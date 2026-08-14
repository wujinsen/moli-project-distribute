package com.moli.knowledge.server.util;

import com.moli.common.exception.BaseException;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Locale;

public final class KbLlmUrlValidator {

    private KbLlmUrlValidator() {
    }

    public static void validateBaseUrl(String baseUrl) {
        if (StringUtils.isBlank(baseUrl)) {
            throw new BaseException("base-url 不能为空");
        }
        String trimmed = baseUrl.trim();
        URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (Exception e) {
            throw new BaseException("base-url 格式非法");
        }
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new BaseException("base-url 须以 http:// 或 https:// 开头");
        }
        String lowerScheme = scheme.toLowerCase(Locale.ROOT);
        if (!"http".equals(lowerScheme) && !"https".equals(lowerScheme)) {
            throw new BaseException("base-url 仅支持 http/https");
        }
        String host = uri.getHost();
        if (StringUtils.isBlank(host)) {
            throw new BaseException("base-url 缺少主机名");
        }
        String lowerHost = host.toLowerCase(Locale.ROOT);
        if ("localhost".equals(lowerHost)
                || "127.0.0.1".equals(lowerHost)
                || lowerHost.startsWith("169.254.")
                || "metadata.google.internal".equals(lowerHost)) {
            throw new BaseException("base-url 不允许指向内网/元数据地址");
        }
    }
}
