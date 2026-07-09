package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OperationPortAuditVo {

    private int total;
    private int matched;
    private int mismatched;
    private int unmapped;
    private int skipped;

    @ApiModelProperty("矩阵条目（权威期望端口）")
    private List<OperationPortMatrixEntryVo> matrix = new ArrayList<>();

    private List<OperationPortAuditItemVo> items = new ArrayList<>();
}
