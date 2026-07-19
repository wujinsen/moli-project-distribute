package com.moli.ai.server.bi.enums;

/**
 * §1.3 请求级错误码（106xx）。
 */
public enum BiChatResponseCode {

    BI_CHAT_QUESTION_INVALID(10601, "问题无效或超长"),
    BI_CHAT_AGENT_UNAVAILABLE(10602, "服务繁忙，请重试"),
    BI_SQL_GENERATION_FAILED(10603, "无法生成安全查询"),
    BI_SQL_EXEC_TIMEOUT(10609, "查询超时，请缩小范围"),
    BI_SQL_EXEC_ROWS_EXCEEDED(10610, "结果行数超限，请缩小范围"),
    BI_CHAT_TRACE_NOT_FOUND(10611, "问答记录不存在"),
    BI_CHAT_TRACE_FORBIDDEN(10612, "无权查看该问答记录"),
    BI_SQL_EXEC_ERROR(10613, "查询执行失败");

    private final int code;
    private final String message;

    BiChatResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
