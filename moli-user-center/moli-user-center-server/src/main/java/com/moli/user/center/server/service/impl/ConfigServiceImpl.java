package com.moli.user.center.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.Constants;
import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.SysConfig;
import com.moli.user.center.common.domain.vo.ConfigItemVo;
import com.moli.user.center.server.config.util.RedisUtil;
import com.moli.user.center.server.mapper.ConfigMapper;
import com.moli.user.center.server.service.ConfigService;
import com.moli.user.center.server.sysparam.ConfigKey;
import com.moli.user.center.server.sysparam.ConfigSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ConfigServiceImpl implements ConfigService {

    /**
     * 缓存中表示「该参数无覆盖行」的哨兵值。
     *
     * <p>没有哨兵的话，未被覆盖的参数每次读取都会穿透到 MySQL —— 而未被覆盖恰恰是
     * 绝大多数参数的常态，那样缓存基本不起作用。
     */
    private static final String NO_OVERRIDE = "__no_override__";

    @Autowired
    private ConfigMapper configMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private Environment environment;

    @Override
    public boolean getBoolean(ConfigKey key) {
        String raw = effectiveValue(key);
        if (!"true".equalsIgnoreCase(raw) && !"false".equalsIgnoreCase(raw)) {
            log.warn("参数 {} 的值 [{}] 无法解析为布尔，回落默认值 {}", key.getKey(), raw, key.getDefaultValue());
            raw = key.getDefaultValue();
        }
        return Boolean.parseBoolean(raw);
    }

    @Override
    public int getInt(ConfigKey key) {
        String raw = effectiveValue(key);
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            log.warn("参数 {} 的值 [{}] 无法解析为整数，回落默认值 {}", key.getKey(), raw, key.getDefaultValue());
            return Integer.parseInt(key.getDefaultValue().trim());
        }
    }

    @Override
    public String getString(ConfigKey key) {
        return effectiveValue(key);
    }

    @Override
    public List<ConfigItemVo> listItems(String groupCode) {
        List<ConfigItemVo> items = new ArrayList<>();
        for (ConfigKey key : ConfigKey.values()) {
            if (StringUtils.isNotBlank(groupCode) && !key.getGroup().name().equals(groupCode)) {
                continue;
            }
            String override = resolveOverride(key.getKey());
            ConfigItemVo vo = new ConfigItemVo();
            vo.setConfigKey(key.getKey());
            vo.setDefaultValue(key.getDefaultValue());
            vo.setValueType(key.getValueType().name());
            vo.setGroupCode(key.getGroup().name());
            vo.setGroupName(key.getGroup().getDisplayName());
            vo.setDescription(key.getDescription());
            vo.setOverridden(override != null);
            if (override != null) {
                vo.setEffectiveValue(override);
                vo.setSource(ConfigSource.DB_OVERRIDE.name());
            } else {
                String fromEnv = environment.getProperty(key.getKey());
                if (fromEnv != null) {
                    vo.setEffectiveValue(fromEnv);
                    vo.setSource(ConfigSource.ENVIRONMENT.name());
                } else {
                    vo.setEffectiveValue(key.getDefaultValue());
                    vo.setSource(ConfigSource.DEFAULT.name());
                }
            }
            items.add(vo);
        }
        return items;
    }

    @Override
    public void setOverride(String configKey, String configValue) {
        ConfigKey declared = requireDeclared(configKey);

        String invalidReason = declared.getValueType().check(configValue);
        if (invalidReason != null) {
            throw new BaseException("参数 " + configKey + " 取值非法：" + invalidReason);
        }

        String normalized = configValue.trim();
        SysConfig existing = selectByKey(configKey);
        if (existing == null) {
            SysConfig row = new SysConfig();
            row.setConfigKey(configKey);
            row.setConfigValue(normalized);
            configMapper.insert(row);
        } else {
            existing.setConfigValue(normalized);
            configMapper.updateById(existing);
        }

        evict(configKey);
        log.info("参数 {} 覆盖值更新为 [{}]", configKey, normalized);
    }

    @Override
    public void resetToDefault(String configKey) {
        requireDeclared(configKey);
        SysConfig existing = selectByKey(configKey);
        if (existing != null) {
            configMapper.deleteById(existing.getId());
        }
        // 覆盖行本来就不存在时同样清缓存：可能缓存里存着哨兵之外的脏值
        evict(configKey);
        log.info("参数 {} 已重置为默认值", configKey);
    }

    /**
     * 取值链的 ①②③④ 四级回落。
     */
    private String effectiveValue(ConfigKey key) {
        String override = resolveOverride(key.getKey());
        if (override != null) {
            return override;
        }
        String fromEnv = environment.getProperty(key.getKey());
        if (fromEnv != null) {
            return fromEnv;
        }
        return key.getDefaultValue();
    }

    /**
     * 取值链 ①②：只缓存「覆盖值查询结果」，不缓存最终生效值。
     *
     * <p>这样缓存的语义与它所缓存的表（sys_config）严格对应；Environment 层保持动态，
     * 未来启用 Nacos 配置刷新时不会被这层缓存挡住。
     *
     * <p>只用 Redis、不做进程内本地缓存：user-center 经 Nacos discovery 可多实例部署，
     * 本地缓存会让 A 实例改完 B 实例仍读旧值，需要额外引入 pub/sub 才能修正。
     * 参数读取频率很低，一次 Redis 往返换掉整个一致性问题是合适的交易。
     */
    private String resolveOverride(String configKey) {
        String cacheKey = Constants.SYS_CONFIG_KEY + configKey;
        try {
            Object cached = redisUtil.get(cacheKey);
            if (cached != null) {
                String cachedValue = String.valueOf(cached);
                return NO_OVERRIDE.equals(cachedValue) ? null : cachedValue;
            }
        } catch (Exception e) {
            // Redis 不可用不应导致参数读取失败，退化为直查 MySQL
            log.warn("读取参数缓存失败，回退查库：{}", e.getMessage());
        }

        SysConfig row = selectByKey(configKey);
        String value = row == null ? null : row.getConfigValue();
        try {
            redisUtil.set(cacheKey, value == null ? NO_OVERRIDE : value);
        } catch (Exception e) {
            log.warn("回填参数缓存失败：{}", e.getMessage());
        }
        return value;
    }

    private SysConfig selectByKey(String configKey) {
        return configMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey));
    }

    private void evict(String configKey) {
        try {
            redisUtil.del(Constants.SYS_CONFIG_KEY + configKey);
        } catch (Exception e) {
            log.warn("清除参数缓存失败，可能读到旧值直至缓存被覆盖：{}", e.getMessage());
        }
    }

    private ConfigKey requireDeclared(String configKey) {
        if (StringUtils.isBlank(configKey)) {
            throw new BaseException("参数键名不能为空");
        }
        Optional<ConfigKey> declared = ConfigKey.find(configKey);
        if (!declared.isPresent()) {
            throw new BaseException("参数 " + configKey + " 未在 ConfigKey 注册表中声明，不能设置。"
                    + "可用参数：" + Arrays.toString(Arrays.stream(ConfigKey.values()).map(ConfigKey::getKey).toArray()));
        }
        return declared.get();
    }
}
