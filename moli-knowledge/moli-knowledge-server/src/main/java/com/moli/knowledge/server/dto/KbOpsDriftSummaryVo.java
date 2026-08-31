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

    @ApiModelProperty("wiki 磁盘页数合计（各空间 wikiPageCount 之和）")
    private int wikiPageTotal;

    @ApiModelProperty("DB 活跃 kb 页数合计（各空间 dbKbPageCount 之和）")
    private int dbKbPageTotal;

    @ApiModelProperty("扫描失败的空间数（wiki 目录不可读等）")
    private int scanFailedCount;

    @ApiModelProperty("已扫描空间但 wiki/DB 均为 0 页（多为 KB_WIKI_ROOT 或 space-dirs 配置问题）")
    private boolean scanEmpty;

    @ApiModelProperty("是否存在漂移")
    private boolean drifted;

    @ApiModelProperty("各空间摘要（单空间查询时仅 1 条）")
    private List<KbDriftReportVo> spaces = new ArrayList<>();
}
