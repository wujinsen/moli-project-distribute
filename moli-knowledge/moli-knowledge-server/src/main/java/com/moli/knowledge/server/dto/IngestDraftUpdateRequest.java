package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 人工改草稿：create 传 content；enrich 可传 patch（推荐）或 content（整页覆盖）。
 */
@Data
@ApiModel("改草稿请求")
public class IngestDraftUpdateRequest {

    @ApiModelProperty("整页内容（create 必填；enrich 可选整页覆盖）")
    private String content;

    @ApiModelProperty("enrich 追加段落 patch（与 content 二选一，优先 patch）")
    private String patch;
}
