package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Sync 批次日趋势点")
public class KbOpsSyncTrendPointVo {

    @ApiModelProperty("日期 yyyy-MM-dd")
    private String date;

    @ApiModelProperty("成功批次数（该日无 fail 行）")
    private int successBatches;

    @ApiModelProperty("失败批次数（该日至少一行 fail）")
    private int failBatches;
}
