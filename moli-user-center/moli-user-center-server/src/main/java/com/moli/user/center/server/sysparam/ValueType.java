package com.moli.user.center.server.sysparam;

import org.apache.commons.lang3.StringUtils;

/**
 * 参数值类型。决定字符串如何被解析，以及写入时如何校验。
 *
 * <p>参数值在 sys_config 表里统一以字符串存储，类型由 {@link ConfigKey} 声明而非由录入人填写，
 * 因此不存在「类型填错导致渲染错」的问题。
 */
public enum ValueType {

    BOOLEAN {
        @Override
        public String checkParsable(String raw) {
            if (!"true".equalsIgnoreCase(raw) && !"false".equalsIgnoreCase(raw)) {
                return "只能是 true 或 false";
            }
            return null;
        }
    },

    INT {
        @Override
        public String checkParsable(String raw) {
            try {
                Integer.parseInt(raw.trim());
                return null;
            } catch (NumberFormatException e) {
                return "只能是整数";
            }
        }
    },

    STRING {
        @Override
        public String checkParsable(String raw) {
            return null;
        }
    };

    /**
     * 校验原始字符串能否按本类型解析。
     *
     * @return 通过返回 {@code null}，否则返回可直接展示给用户的原因
     */
    public abstract String checkParsable(String raw);

    /**
     * 统一的空值判定：空白字符串一律视为非法，
     * 「没有覆盖值」应通过删除 sys_config 行表达，而不是写入空串。
     */
    public String check(String raw) {
        if (StringUtils.isBlank(raw)) {
            return "参数值不能为空；如需恢复默认值请使用重置";
        }
        return checkParsable(raw);
    }
}
