package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("Dashboard · wiki↔DB 漂移摘要")
public class KbOpsDriftSummaryVo {

    @ApiModelProperty("检测时间")
    private Date checkedAt;

    @ApiModelProperty("存在漂移的空间数")
    private int spacesWithDrift;

    @ApiModelProperty("扫描的空间数")
    private int spacesScanned;

    @ApiModelProperty("wikiOnly 合计")
    private int wikiOnlyTotal;

    @ApiModelProperty("dbOnly 合计")
    private int dbOnlyTotal;

    @ApiModelProperty("hashMismatch 合计")
    private int hashMismatchTotal;

    @ApiModelProperty("inSync 合计")
    private int inSyncTotal;

    @ApiModelProperty("是否存在漂移")
    private boolean drifted;

    @ApiModelProperty("各空间摘要（单空间查询时仅 1 条）")
    private List<KbDriftReportVo> spaces = new ArrayList<>();
}
