package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("wiki↔DB 漂移报告（KBOPS-A3）")
public class KbDriftReportVo {

    @ApiModelProperty("空间 ID")
    private Long spaceId;

    @ApiModelProperty("空间 code")
    private String spaceCode;

    @ApiModelProperty("wiki 子目录（相对 kb/）")
    private String wikiDir;

    @ApiModelProperty("检测时间")
    private Date checkedAt;

    @ApiModelProperty("wiki 页数")
    private int wikiPageCount;

    @ApiModelProperty("DB 活跃 kb 页数")
    private int dbKbPageCount;

    @ApiModelProperty("hash 一致")
    private int inSyncCount;

    @ApiModelProperty("wiki 有、DB 无（待 Sync）")
    private int wikiOnlyCount;

    @ApiModelProperty("DB 有、wiki 无（待删或手改 DB）")
    private int dbOnlyCount;

    @ApiModelProperty("hash 不一致（改 wiki 未 Sync 或手改 DB）")
    private int hashMismatchCount;

    @ApiModelProperty("是否存在漂移")
    private boolean drifted;

    @ApiModelProperty("样本上限")
    private int sampleLimit;

    @ApiModelProperty("wikiOnly 样本")
    private List<KbDriftItemVo> wikiOnly = new ArrayList<>();

    @ApiModelProperty("dbOnly 样本")
    private List<KbDriftItemVo> dbOnly = new ArrayList<>();

    @ApiModelProperty("hashMismatch 样本")
    private List<KbDriftItemVo> hashMismatches = new ArrayList<>();
}
