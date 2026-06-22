package com.moli.knowledge.server.enums;

import lombok.Getter;

@Getter
public enum DocumentStatus {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    ARCHIVED(2, "已归档");

    private final int code;
    private final String label;

    DocumentStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }
}
