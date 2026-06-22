package com.moli.user.center.starter.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "moli.user-center.shiro")
public class UserCenterShiroProperties {

    private boolean enabled = true;

    private int sessionExpireSeconds = 86400;

    /**
     * Extra paths that skip Shiro auth (e.g. /seckill/** for load-test endpoints).
     */
    private List<String> anonPaths = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getSessionExpireSeconds() {
        return sessionExpireSeconds;
    }

    public void setSessionExpireSeconds(int sessionExpireSeconds) {
        this.sessionExpireSeconds = sessionExpireSeconds;
    }

    public List<String> getAnonPaths() {
        return anonPaths;
    }

    public void setAnonPaths(List<String> anonPaths) {
        this.anonPaths = anonPaths;
    }
}
