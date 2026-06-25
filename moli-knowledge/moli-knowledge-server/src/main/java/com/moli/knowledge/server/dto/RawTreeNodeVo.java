package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * raw 目录树节点（只读）。dir 节点含 children；file 节点带大小。
 */
@Data
@ApiModel("raw 目录树节点")
public class RawTreeNodeVo {

    @ApiModelProperty("节点名（文件名/目录名）")
    private String name;

    @ApiModelProperty("相对 rawRoot 的路径")
    private String path;

    @ApiModelProperty("dir / file")
    private String type;

    @ApiModelProperty("文件字节大小（file）")
    private Long size;

    @ApiModelProperty("子节点（dir）")
    private List<RawTreeNodeVo> children;
}
