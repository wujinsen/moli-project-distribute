package com.moli.user.center.server.sysparam;

/**
 * 生效值的来源，对应 {@code ConfigService} 取值链命中的层级。
 *
 * <p>UI 需要展示来源，否则运维无法判断「这个值是我改的，还是部署时就这样」。
 */
public enum ConfigSource {

    /** sys_config 表存在覆盖行 */
    DB_OVERRIDE,

    /** 无覆盖行，取自 Spring Environment（yaml，未来可能是 Nacos） */
    ENVIRONMENT,

    /** 既无覆盖行也未在 Environment 中配置，取 ConfigKey 声明的默认值 */
    DEFAULT
}
