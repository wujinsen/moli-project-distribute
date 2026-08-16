package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 参数列表项：注册表声明 + 当前生效情况的合并视图。
 *
 * <p>列表来源是**注册表**而非 sys_config 表，因此从未被覆盖过的参数也会出现，
 * 运维第一次进页面就能看到系统有哪些旋钮，而不是一张空表。
 */
@Data
public class ConfigItemVo {

    @ApiModelProperty(value = "参数键名")
    private String configKey;

    @ApiModelProperty(value = "生效值")
    private String effectiveValue;

    @ApiModelProperty(value = "声明的默认值")
    private String defaultValue;

    @ApiModelProperty(value = "值类型：BOOLEAN / INT / STRING，前端据此渲染控件")
    private String valueType;

    @ApiModelProperty(value = "分组编码")
    private String groupCode;

    @ApiModelProperty(value = "分组展示名")
    private String groupName;

    @ApiModelProperty(value = "参数说明")
    private String description;

    @ApiModelProperty(value = "生效值来源：DB_OVERRIDE / ENVIRONMENT / DEFAULT")
    private String source;

    @ApiModelProperty(value = "是否存在运行期覆盖值。true 时前端展示「重置为默认」")
    private Boolean overridden;

}
