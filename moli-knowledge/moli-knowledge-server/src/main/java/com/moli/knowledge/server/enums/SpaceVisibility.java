package com.moli.knowledge.server.enums;

import lombok.Getter;

@Getter
public enum SpaceVisibility {

    PRIVATE(0, "私有"),
    INTERNAL(1, "内部"),
    PUBLIC(2, "公开");

    private final int code;
    private final String label;

    SpaceVisibility(int code, String label) {
        this.code = code;
        this.label = label;
    }
}
