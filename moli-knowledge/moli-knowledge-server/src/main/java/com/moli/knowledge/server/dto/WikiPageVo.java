package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("Wiki 文件内容（在线编辑读取）")
public class WikiPageVo {

    @ApiModelProperty("slug（相对 wiki 目录、去扩展名）")
    private String slug;

    @ApiModelProperty("所属空间ID")
    private Long spaceId;

    @ApiModelProperty("空间编码")
    private String spaceCode;

    @ApiModelProperty("wiki 文件相对路径（root 起）")
    private String relativePath;

    @ApiModelProperty("文件全文（frontmatter + 正文）")
    private String content;

    @ApiModelProperty("内容 SHA-256（乐观锁/变更比对）")
    private String contentHash;

    @ApiModelProperty("文件是否已存在（false=可新建）")
    private boolean exists;

    @ApiModelProperty("文件最后修改时间")
    private Date updatedAt;
}
