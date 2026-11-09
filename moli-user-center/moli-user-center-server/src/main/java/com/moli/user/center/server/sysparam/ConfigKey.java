package com.moli.user.center.server.sysparam;

import java.util.Arrays;
import java.util.Optional;

/**
 * 系统参数注册表 —— 参数的**唯一定义处**。
 *
 * <p>设计：{@code docs/design/sys-config-notice.md} §2、§3.2。
 *
 * <p>为什么参数定义在代码而不在数据库：
 * <ul>
 *   <li>只有代码里有读取参数的地方，DB 里凭空多一个 key 就是死数据；</li>
 *   <li>参数的类型与默认值是代码知识，写进 DB 会与实际读取逻辑不一致；</li>
 *   <li>有了声明才能在写入时校验值是否合法（通用键值表不知道 key 意味着什么）。</li>
 * </ul>
 *
 * <p>{@code key} 沿用现有 yaml 路径（如 {@code captcha.enabled}），
 * 使取值链能直接回落到 Spring Environment，存量 yaml 与部署脚本无需改动。
 *
 * <p><b>新增参数的步骤</b>：在此处加一个枚举常量 → 在使用处改为 {@code configService.getXxx(ConfigKey.X)}。
 * 不需要写 SQL，也不需要在页面上新建 —— 参数不能由 UI 创建。
 */
public enum ConfigKey {

    CAPTCHA_ENABLED(
            "captcha.enabled",
            ValueType.BOOLEAN,
            "false",
            ConfigGroup.SECURITY,
            "登录验证码开关。关闭后 /captchaImage 返回提示且登录不校验验证码"),

    SSO_ENABLED(
            "sso.enabled",
            ValueType.BOOLEAN,
            "true",
            ConfigGroup.PORTAL,
            "多系统门户开关。关闭后登录直接下发菜单，不走选系统页"),

    OPS_COMMAND_ENABLED(
            "ops.command.enabled",
            ValueType.BOOLEAN,
            "false",
            ConfigGroup.OPS,
            "运维远程命令开关。属安全开关，事故时需要能立即关停"),

    OPS_HEALTH_PROBE_ENABLED(
            "ops.health.probe-enabled",
            ValueType.BOOLEAN,
            "true",
            ConfigGroup.OPS,
            "服务器健康巡检定时任务开关"),

    OPS_ALERT_EMAIL_ENABLED(
            "ops.alert.email.enabled",
            ValueType.BOOLEAN,
            "false",
            ConfigGroup.OPS,
            "Java 服务宕机邮件告警。打开后，Prometheus MoliServiceDown 会发到下方收件人"),

    OPS_ALERT_EMAIL_TO(
            "ops.alert.email.to",
            ValueType.STRING,
            "",
            ConfigGroup.OPS,
            "告警收件邮箱，多个用逗号或分号分隔"),

    OPS_ALERT_EMAIL_FROM(
            "ops.alert.email.from",
            ValueType.STRING,
            "",
            ConfigGroup.OPS,
            "发件人地址。留空则使用 SMTP 用户名"),

    OPS_ALERT_SMTP_HOST(
            "ops.alert.smtp.host",
            ValueType.STRING,
            "",
            ConfigGroup.OPS,
            "SMTP 服务器，如 smtp.qq.com / smtp.163.com"),

    OPS_ALERT_SMTP_PORT(
            "ops.alert.smtp.port",
            ValueType.INT,
            "465",
            ConfigGroup.OPS,
            "SMTP 端口。QQ/网易常用 465（SSL）或 587（STARTTLS）"),

    OPS_ALERT_SMTP_USERNAME(
            "ops.alert.smtp.username",
            ValueType.STRING,
            "",
            ConfigGroup.OPS,
            "SMTP 用户名，一般为完整邮箱"),

    OPS_ALERT_SMTP_PASSWORD(
            "ops.alert.smtp.password",
            ValueType.SECRET,
            "",
            ConfigGroup.OPS,
            "SMTP 密码或授权码（QQ/163 需在邮箱设置里开 SMTP 并生成授权码）"),

    OPS_ALERT_SMTP_SSL(
            "ops.alert.smtp.ssl",
            ValueType.BOOLEAN,
            "true",
            ConfigGroup.OPS,
            "SMTP 使用 SSL（端口 465 请开；587 可关并改用 STARTTLS）");

    private final String key;
    private final ValueType valueType;
    private final String defaultValue;
    private final ConfigGroup group;
    private final String description;

    ConfigKey(String key, ValueType valueType, String defaultValue, ConfigGroup group, String description) {
        this.key = key;
        this.valueType = valueType;
        this.defaultValue = defaultValue;
        this.group = group;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public ConfigGroup getGroup() {
        return group;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 按 key 查声明。写入接口用它拦掉未声明的 key，避免 sys_config 里出现无人读取的垃圾行。
     */
    public static Optional<ConfigKey> find(String key) {
        return Arrays.stream(values())
                .filter(it -> it.key.equals(key))
                .findFirst();
    }

    /**
     * 参数分组，用于 UI 左侧分区。分组是声明的一部分，不由录入人填。
     */
    public enum ConfigGroup {
        SECURITY("安全"),
        PORTAL("门户与单点登录"),
        OPS("运维");

        private final String displayName;

        ConfigGroup(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
