package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("raw 覆盖项")
public class RawCoverageItemVo {

    @ApiModelProperty("相对 rawRoot 的路径")
    private String path;

    @ApiModelProperty("open | covered | cluster")
    private String coverage;

    @ApiModelProperty("exact | dir_prefix | none")
    private String matchKind;

    @ApiModelProperty("引用该 raw 的 wiki slug 列表")
    private List<String> wikiSlugs = new ArrayList<>();

    @ApiModelProperty("进行中批次 job id（未 committed/cancelled）")
    private List<Long> inFlightJobIds = new ArrayList<>();
}
