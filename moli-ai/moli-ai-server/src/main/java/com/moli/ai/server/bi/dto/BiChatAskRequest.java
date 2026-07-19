package com.moli.ai.server.bi.dto;

import lombok.Data;

@Data
public class BiChatAskRequest {

    private String sessionId;
    private String question;
    private Boolean stream;
    private Integer maxRows;
}
