package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Wiki enrich 单页结果")
public class WikiEnrichItemResultVo {

    private String slug;
    private String patch;
    private String mergedPreview;
    private Boolean applied;
    private String error;
}
