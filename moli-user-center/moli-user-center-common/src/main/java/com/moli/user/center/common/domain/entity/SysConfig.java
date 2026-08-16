package com.moli.user.center.common.domain.entity;

import com.moli.common.core.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 系统参数**运行期覆盖值**。
 *
 * <p>本实体只承载「某个已声明参数被改成了什么」。参数的名称、类型、默认值、分组、说明
 * 全部在 {@code ConfigKey} 注册表里声明，不落库 —— 详见 {@code docs/design/sys-config-notice.md} §2。
 *
 * <p>因此这里没有 configName / valueType / configType / status / groupCode / remark 字段。
 * 一行不存在即表示该参数取默认值。
 */
@Data
public class SysConfig extends BaseEntity {

    @ApiModelProperty(value = "参数键名，必须是 ConfigKey 注册表中已声明的 key")
    private String configKey;

    @ApiModelProperty(value = "覆盖值，按声明的 valueType 解析")
    private String configValue;

}
