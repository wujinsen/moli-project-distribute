package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@ApiModel("工作流下一步提示（供前端展示 CTA）")
public class KbWorkflowHintVo {

    @ApiModelProperty("稳定键：wiki_govern_lint | kb_health_scan")
    private String key;

    @ApiModelProperty("按钮/链接文案")
    private String label;

    @ApiModelProperty("说明（可选）")
    private String description;

    @ApiModelProperty("前端路由 path，如 knowledge/wiki-govern/index")
    private String routePath;

    @ApiModelProperty("路由 query，如 spaceId")
    private Map<String, String> routeQuery = new LinkedHashMap<>();
}
