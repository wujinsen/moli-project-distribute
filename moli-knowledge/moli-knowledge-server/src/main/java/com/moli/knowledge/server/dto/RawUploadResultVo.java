package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("Raw 投喂上传结果（T20a）")
public class RawUploadResultVo {

    @ApiModelProperty("成功上传")
    private List<RawUploadItemVo> uploaded = new ArrayList<>();

    @ApiModelProperty("跳过")
    private List<RawUploadSkippedVo> skipped = new ArrayList<>();

    @ApiModelProperty("重命名后写入")
    private List<RawUploadRenamedVo> renamed = new ArrayList<>();
}
