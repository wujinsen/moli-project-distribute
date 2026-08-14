package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Wiki 文件保存请求")
public class WikiSaveRequest {

    @ApiModelProperty(value = "slug（相对 wiki 目录、去扩展名）", required = true)
    private String slug;

    @ApiModelProperty("所属空间ID（省略=默认 enterprise-kb）")
    private Long spaceId;

    @ApiModelProperty(value = "文件全文（frontmatter + 正文）", required = true)
    private String content;

    @ApiModelProperty("变更说明（审计/日志用，可选）")
    private String changeLog;

    @ApiModelProperty("打开时的 contentHash；非空时做乐观锁，冲突返回 409 语义异常")
    private String baselineHash;
}
