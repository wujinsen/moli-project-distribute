package com.moli.knowledge.server.exception;

import com.moli.common.enums.ResponseCodeEnums;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.dto.IngestRawConflictVo;
import lombok.Getter;

/**
 * Ingest commit 时 raw 已被其它 wiki 页 sources 引用（非本批 enrich 同一 slug）。
 */
@Getter
public class IngestRawConflictException extends BaseException {

    private static final long serialVersionUID = 1L;

    private final IngestRawConflictVo detail;

    public IngestRawConflictException(String message, IngestRawConflictVo detail) {
        super(ResponseCodeEnums.BIZ_ERROR_CODE.getCode(), message);
        this.detail = detail;
    }
}
