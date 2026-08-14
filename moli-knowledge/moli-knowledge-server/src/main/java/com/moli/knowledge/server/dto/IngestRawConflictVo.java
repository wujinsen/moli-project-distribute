package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("Ingest commit raw 覆盖冲突详情")
public class IngestRawConflictVo {

    public static final String ERROR_KIND = "INGEST_RAW_ALREADY_COVERED";

    @ApiModelProperty("错误类型，前端可据此分支展示")
    private String errorKind = ERROR_KIND;

    @ApiModelProperty("空间 ID")
    private Long spaceId;

    @ApiModelProperty("批次 job ID")
    private Long jobId;

    @ApiModelProperty("冲突项（字段同 GET /kb/ingest/raw-coverage items）")
    private List<RawCoverageItemVo> conflicts = new ArrayList<>();

    @ApiModelProperty("建议操作")
    private String hint = "请对已有页 enrich 或更换 raw 源。";
}
