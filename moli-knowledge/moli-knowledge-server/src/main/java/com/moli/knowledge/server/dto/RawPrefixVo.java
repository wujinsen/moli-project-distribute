package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * raw 下已有的一级 prefix（相对 {@code kb/raw/}），供 Tab1 下拉。
 */
@Data
@ApiModel("raw 一级 prefix")
public class RawPrefixVo {

    @ApiModelProperty("相对 raw 根的路径，如 test-walkthrough、school")
    private String prefix;
}
