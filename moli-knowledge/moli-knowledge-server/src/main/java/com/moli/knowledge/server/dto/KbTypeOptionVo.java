package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 体裁白名单选项：供前端「体裁下拉/筛选」数据源（单一来源 {@code KbTypeConstants}）。
 */
@Data
@ApiModel("体裁选项")
public class KbTypeOptionVo {

    @ApiModelProperty("体裁值")
    private String value;

    @ApiModelProperty("中文展示名")
    private String label;

    public KbTypeOptionVo() {
    }

    public KbTypeOptionVo(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
