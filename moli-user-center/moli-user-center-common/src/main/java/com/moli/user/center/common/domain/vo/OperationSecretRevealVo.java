package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationSecretRevealVo {

    @ApiModelProperty("明文凭据（仅 operation:secret:view 可获取）")
    private String password;
}
