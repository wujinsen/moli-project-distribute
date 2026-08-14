package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("raw 覆盖索引（wiki sources 反向映射）")
public class RawCoverageVo {

    @ApiModelProperty("目标空间 ID")
    private Long spaceId;

    @ApiModelProperty("space_code")
    private String spaceCode;

    @ApiModelProperty("wiki 子目录（相对 kb/）")
    private String wikiDir;

    @ApiModelProperty("索引构建时间")
    private Date indexedAt;

    @ApiModelProperty("参与索引的 wiki 页数")
    private int wikiPageCount;

    @ApiModelProperty("请求 filter：all | open | covered | cluster")
    private String filter;

    @ApiModelProperty("统计")
    private RawCoverageSummaryVo summary;

    @ApiModelProperty("文件级覆盖项（受 filter 影响）")
    private List<RawCoverageItemVo> items = new ArrayList<>();
}
