package com.moli.ai.server.bi.security;

import lombok.Data;

@Data
public class BiSqlValidationResult {

    private boolean passed;
    private BiSqlRejectCode rejectCode;
    private String rejectReason;
    /** 注入/钳制 LIMIT 后的可执行 SQL；拒答时为 null */
    private String sanitizedSql;

    public static BiSqlValidationResult pass(String sanitizedSql) {
        BiSqlValidationResult r = new BiSqlValidationResult();
        r.passed = true;
        r.sanitizedSql = sanitizedSql;
        return r;
    }

    public static BiSqlValidationResult reject(BiSqlRejectCode code, String reason) {
        BiSqlValidationResult r = new BiSqlValidationResult();
        r.passed = false;
        r.rejectCode = code;
        r.rejectReason = reason;
        return r;
    }
}
