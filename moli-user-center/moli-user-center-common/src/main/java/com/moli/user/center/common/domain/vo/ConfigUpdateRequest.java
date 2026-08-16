package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 写参数覆盖值的请求体。
 *
 * <p>不复用 {@code SysConfig} 实体作为入参：实体带 id 与审计字段，
 * 直接接收会给前端「可以指定 id / 改创建人」的错误暗示。这里只暴露该改的两个字段。
 */
@Data
public class ConfigUpdateRequest {

    @ApiModelProperty(value = "参数键名，必须是已声明的 key", required = true)
    private String configKey;

    @ApiModelProperty(value = "覆盖值", required = true)
    private String configValue;

}
