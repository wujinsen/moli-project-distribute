package com.moli.user.center.common.domain.dto.operation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class OperationPortMatrixSaveRequest {

    @ApiModelProperty("主键（更新时必填）")
    private Long id;

    @NotBlank(message = "matrixKey 不能为空")
    @Pattern(regexp = "^[a-z][a-z0-9-]{0,63}$", message = "matrixKey 格式非法")
    private String matrixKey;

    @Size(max = 128, message = "displayName 过长")
    private String displayName;

    @NotBlank(message = "expectedPort 不能为空")
    @Size(max = 16, message = "expectedPort 过长")
    private String expectedPort;

    @ApiModelProperty("别名全量替换")
    private List<String> aliases;

    private Integer sortOrder;

    private Boolean enabled;

    @Size(max = 256, message = "source 过长")
    private String source;

    @Size(max = 512, message = "remark 过长")
    private String remark;
}
