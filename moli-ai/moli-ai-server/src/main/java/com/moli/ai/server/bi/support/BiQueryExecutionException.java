package com.moli.ai.server.bi.support;

import com.moli.ai.server.bi.enums.BiChatResponseCode;
import lombok.Getter;

@Getter
public class BiQueryExecutionException extends Exception {

    private final BiChatResponseCode code;

    public BiQueryExecutionException(BiChatResponseCode code, String message) {
        super(message);
        this.code = code;
    }
}
