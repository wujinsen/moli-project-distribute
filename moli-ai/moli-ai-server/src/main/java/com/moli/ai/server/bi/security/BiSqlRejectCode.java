package com.moli.ai.server.bi.security;

/**
 * §1.3 拒答码（status=REJECTED 时）；校验顺序见 bi-chatbi-nl2sql-contract §3.2。
 */
public enum BiSqlRejectCode {

    REJECT_NON_SELECT("REJECT_NON_SELECT"),
    REJECT_MULTI_STATEMENT("REJECT_MULTI_STATEMENT"),
    REJECT_TABLE_NOT_ALLOWED("REJECT_TABLE_NOT_ALLOWED"),
    REJECT_COLUMN_BLOCKED("REJECT_COLUMN_BLOCKED"),
    REJECT_STAR_SELECT("REJECT_STAR_SELECT"),
    REJECT_DANGEROUS("REJECT_DANGEROUS"),
    REJECT_SEMANTIC("REJECT_SEMANTIC");

    private final String code;

    BiSqlRejectCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
