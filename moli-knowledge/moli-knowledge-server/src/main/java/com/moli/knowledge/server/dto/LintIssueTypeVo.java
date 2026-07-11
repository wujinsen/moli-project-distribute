package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("体检问题类型（KBOPS-8/10 · 与 lint.py 对照）")
public class LintIssueTypeVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("Web / kb_lint_issue.issue_type")
    private String code;

    @ApiModelProperty("中文说明")
    private String label;

    @ApiModelProperty("lint.py KIND 名（无则 null）")
    private String lintPyKind;

    @ApiModelProperty("仅 Web DB 体检（lint.py 无等价项）")
    private boolean webOnly;

    @ApiModelProperty("仅 lint.py 文件体检（Web 不扫）")
    private boolean lintPyOnly;
}
