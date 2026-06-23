package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("空间成员批量操作结果")
public class KbSpaceMemberBatchResult {

    @ApiModelProperty("成功数（新增或恢复）")
    private int successCount;

    @ApiModelProperty("跳过数（已是有效成员）")
    private int skipCount;

    @ApiModelProperty("失败数")
    private int failCount;

    @ApiModelProperty("成功写入的成员行 ID（kb_space_member.id）")
    private List<Long> memberRowIds = new ArrayList<>();
}
