package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("Wiki 文件保存结果")
public class WikiSaveResultVo {

    @ApiModelProperty("slug")
    private String slug;

    @ApiModelProperty("所属空间ID")
    private Long spaceId;

    @ApiModelProperty("wiki 文件相对路径（root 起）")
    private String relativePath;

    @ApiModelProperty("是否新建文件")
    private boolean created;

    @ApiModelProperty("保存后内容 SHA-256")
    private String contentHash;

    @ApiModelProperty("保存时间")
    private Date savedAt;
}
