package com.moli.knowledge.server.dto;

import lombok.Data;

/**
 * 体检工单分页查询参数（内部）。
 */
@Data
public class LintIssuePageQuery {

    private Long spaceId;
    private Integer status;
    private String issueType;
    private Long assigneeId;
    private Integer priority;
    private boolean unassignedOnly;
    private int pageNum = 1;
    private int pageSize = 20;
}
