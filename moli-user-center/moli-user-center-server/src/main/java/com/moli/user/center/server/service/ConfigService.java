package com.moli.user.center.server.service;

import com.moli.user.center.common.domain.vo.ConfigItemVo;
import com.moli.user.center.server.sysparam.ConfigKey;

import java.util.List;

/**
 * 系统参数读写。业务代码**只应通过本接口取参数**，不要自己拼 Redis key 或直连 sys_config，
 * 否则缓存 key 规则会随调用点漂移。
 *
 * <p>取值链（详见 {@code docs/design/sys-config-notice.md} §3.3）：
 * <pre>
 *   ① Redis  sys_config:{key}
 *   ② MySQL  sys_config           命中则回填 Redis
 *   ③ Spring Environment          yaml / 未来 Nacos
 *   ④ ConfigKey 声明的默认值
 * </pre>
 */
public interface ConfigService {

    /**
     * 取布尔参数。解析失败时回落到声明的默认值并记警告，
     * 不抛异常 —— 一个坏参数值不应让登录之类的主流程整体不可用。
     */
    boolean getBoolean(ConfigKey key);

    int getInt(ConfigKey key);

    String getString(ConfigKey key);

    /**
     * 参数列表：注册表声明 ∪ 当前覆盖情况。
     *
     * @param groupCode 分组过滤，为空则返回全部
     */
    List<ConfigItemVo> listItems(String groupCode);

    /**
     * 写入覆盖值。校验 key 已声明、值可按声明类型解析，任一不满足抛 {@code BaseException}。
     */
    void setOverride(String configKey, String configValue);

    /**
     * 重置为默认：删除覆盖行。语义不是「删除参数」——参数由代码声明，始终存在。
     */
    void resetToDefault(String configKey);
}
