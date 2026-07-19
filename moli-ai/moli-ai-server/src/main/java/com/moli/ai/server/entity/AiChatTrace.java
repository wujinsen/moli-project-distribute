package com.moli.ai.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("ai_chat_trace")
public class AiChatTrace implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String traceId;
    private String sessionId;
    private Long userId;
    private String question;
    private String finalSql;
    private String status;
    private String rejectCode;
    private String rejectReason;
    private Integer rowCount;
    private Long latencyMs;
    private Integer retry;
    private String stepsJson;
    private Date createdAt;
}
