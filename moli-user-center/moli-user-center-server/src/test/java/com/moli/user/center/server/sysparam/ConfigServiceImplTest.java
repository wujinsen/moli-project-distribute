package com.moli.user.center.server.sysparam;

import com.moli.common.constant.Constants;
import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.SysConfig;
import com.moli.user.center.common.domain.vo.ConfigItemVo;
import com.moli.user.center.server.config.util.RedisUtil;
import com.moli.user.center.server.mapper.ConfigMapper;
import com.moli.user.center.server.service.impl.ConfigServiceImpl;
import com.moli.user.center.server.testsupport.AbstractApiTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 取值链四级回落 + 写入校验。
 *
 * <p>这些用例是设计成立与否的判据：如果四级回落错了，参数改了不生效或改了生效不了都会发生。
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigServiceImplTest extends AbstractApiTest {

    @InjectMocks
    private ConfigServiceImpl configService;

    @Mock
    private ConfigMapper configMapper;

    @Mock
    private RedisUtil redisUtil;

    private final MockEnvironment environment = new MockEnvironment();

    private void useEnvironment() {
        ReflectionTestUtils.setField(configService, "environment", environment);
    }

    // ---------- ① Redis 命中 ----------

    @Test
    public void getBoolean_hitsRedisOverride_withoutQueryingDb() {
        useEnvironment();
        when(redisUtil.get(Constants.SYS_CONFIG_KEY + "captcha.enabled")).thenReturn("true");

        Assert.assertTrue(configService.getBoolean(ConfigKey.CAPTCHA_ENABLED));
        verify(configMapper, never()).selectOne(any());
    }

    /** 哨兵命中：Redis 记录了「无覆盖行」，同样不应再查库 */
    @Test
    public void getBoolean_hitsRedisSentinel_fallsBackWithoutQueryingDb() {
        useEnvironment();
        environment.setProperty("captcha.enabled", "true");
        when(redisUtil.get(anyString())).thenReturn("__no_override__");

        Assert.assertTrue(configService.getBoolean(ConfigKey.CAPTCHA_ENABLED));
        verify(configMapper, never()).selectOne(any());
    }

    // ---------- ② MySQL 覆盖值 ----------

    @Test
    public void getBoolean_readsDbOverride_andBackfillsCache() {
        useEnvironment();
        when(redisUtil.get(anyString())).thenReturn(null);
        SysConfig row = new SysConfig();
        row.setConfigKey("captcha.enabled");
        row.setConfigValue("true");
        when(configMapper.selectOne(any())).thenReturn(row);

        Assert.assertTrue(configService.getBoolean(ConfigKey.CAPTCHA_ENABLED));
        verify(redisUtil).set(Constants.SYS_CONFIG_KEY + "captcha.enabled", "true");
    }

    /** 无覆盖行时应把哨兵写进缓存，否则未被覆盖的参数每次读都穿透到 MySQL */
    @Test
    public void getBoolean_noOverride_cachesSentinel() {
        useEnvironment();
        when(redisUtil.get(anyString())).thenReturn(null);
        when(configMapper.selectOne(any())).thenReturn(null);

        configService.getBoolean(ConfigKey.CAPTCHA_ENABLED);
        verify(redisUtil).set(Constants.SYS_CONFIG_KEY + "captcha.enabled", "__no_override__");
    }

    // ---------- ③ Environment（yaml / 未来 Nacos）----------

    @Test
    public void getBoolean_fallsBackToEnvironment_whenNoDbOverride() {
        useEnvironment();
        // captcha.enabled 声明默认是 false，yaml 配成 true 应生效
        environment.setProperty("captcha.enabled", "true");
        when(redisUtil.get(anyString())).thenReturn(null);
        when(configMapper.selectOne(any())).thenReturn(null);

        Assert.assertTrue(configService.getBoolean(ConfigKey.CAPTCHA_ENABLED));
    }

    /** DB 覆盖值优先级高于 Environment —— 这是「运行期可改」的核心 */
    @Test
    public void dbOverride_winsOverEnvironment() {
        useEnvironment();
        environment.setProperty("captcha.enabled", "true");
        when(redisUtil.get(anyString())).thenReturn("false");

        Assert.assertFalse(configService.getBoolean(ConfigKey.CAPTCHA_ENABLED));
    }

    // ---------- ④ 声明默认值 ----------

    @Test
    public void getBoolean_fallsBackToDeclaredDefault() {
        useEnvironment();
        when(redisUtil.get(anyString())).thenReturn(null);
        when(configMapper.selectOne(any())).thenReturn(null);

        // SSO_ENABLED 声明默认 true，且 Environment 未配置
        Assert.assertTrue(configService.getBoolean(ConfigKey.SSO_ENABLED));
        // CAPTCHA_ENABLED 声明默认 false
        Assert.assertFalse(configService.getBoolean(ConfigKey.CAPTCHA_ENABLED));
    }

    /** 坏值不应让登录之类的主流程炸掉，而是回落默认值 */
    @Test
    public void getBoolean_unparsableValue_fallsBackToDefault() {
        useEnvironment();
        when(redisUtil.get(anyString())).thenReturn("yes-please");

        Assert.assertTrue(configService.getBoolean(ConfigKey.SSO_ENABLED));
    }

    // ---------- 写入校验 ----------

    @Test(expected = BaseException.class)
    public void setOverride_rejectsUndeclaredKey() {
        configService.setOverride("some.random.key", "true");
    }

    @Test(expected = BaseException.class)
    public void setOverride_rejectsValueNotMatchingDeclaredType() {
        configService.setOverride("captcha.enabled", "maybe");
    }

    @Test(expected = BaseException.class)
    public void setOverride_rejectsBlankValue() {
        configService.setOverride("captcha.enabled", "   ");
    }

    @Test
    public void setOverride_insertsAndEvictsCache() {
        when(configMapper.selectOne(any())).thenReturn(null);
        when(configMapper.insert(any())).thenReturn(1);

        configService.setOverride("captcha.enabled", "true");

        verify(configMapper).insert(any(SysConfig.class));
        verify(redisUtil).del(Constants.SYS_CONFIG_KEY + "captcha.enabled");
    }

    @Test
    public void setOverride_updatesExistingRow() {
        SysConfig existing = new SysConfig();
        existing.setId(1L);
        existing.setConfigKey("captcha.enabled");
        existing.setConfigValue("false");
        when(configMapper.selectOne(any())).thenReturn(existing);
        when(configMapper.updateById(any())).thenReturn(1);

        configService.setOverride("captcha.enabled", "true");

        verify(configMapper).updateById(any(SysConfig.class));
        verify(configMapper, never()).insert(any());
    }

    // ---------- 重置为默认 ----------

    @Test
    public void resetToDefault_deletesOverrideRowAndEvicts() {
        SysConfig existing = new SysConfig();
        existing.setId(9L);
        existing.setConfigKey("captcha.enabled");
        when(configMapper.selectOne(any())).thenReturn(existing);
        when(configMapper.deleteById(any(java.io.Serializable.class))).thenReturn(1);

        configService.resetToDefault("captcha.enabled");

        verify(configMapper).deleteById(9L);
        verify(redisUtil).del(Constants.SYS_CONFIG_KEY + "captcha.enabled");
    }

    /** 覆盖行本来就不存在时不报错，但仍清缓存（可能缓存里是脏值） */
    @Test
    public void resetToDefault_isIdempotentWhenNoOverride() {
        when(configMapper.selectOne(any())).thenReturn(null);

        configService.resetToDefault("captcha.enabled");

        verify(configMapper, never()).deleteById(any(java.io.Serializable.class));
        verify(redisUtil, times(1)).del(Constants.SYS_CONFIG_KEY + "captcha.enabled");
    }

    @Test(expected = BaseException.class)
    public void resetToDefault_rejectsUndeclaredKey() {
        configService.resetToDefault("some.random.key");
    }

    // ---------- 列表 ----------

    /** 列表来自注册表而非表，因此空表也应返回全部已声明参数 */
    @Test
    public void listItems_returnsAllDeclaredKeys_evenWithEmptyTable() {
        useEnvironment();
        when(redisUtil.get(anyString())).thenReturn(null);
        when(configMapper.selectOne(any())).thenReturn(null);

        List<ConfigItemVo> items = configService.listItems(null);

        Assert.assertEquals(ConfigKey.values().length, items.size());
        for (ConfigItemVo item : items) {
            Assert.assertFalse(item.getOverridden());
            Assert.assertEquals(ConfigSource.DEFAULT.name(), item.getSource());
            Assert.assertNotNull(item.getValueType());
            Assert.assertNotNull(item.getGroupName());
        }
    }

    @Test
    public void listItems_marksSourceAndOverriddenFlag() {
        useEnvironment();
        environment.setProperty("sso.enabled", "false");
        when(redisUtil.get(Constants.SYS_CONFIG_KEY + "captcha.enabled")).thenReturn("true");
        when(redisUtil.get(Constants.SYS_CONFIG_KEY + "sso.enabled")).thenReturn("__no_override__");
        when(redisUtil.get(Constants.SYS_CONFIG_KEY + "ops.command.enabled")).thenReturn("__no_override__");
        when(redisUtil.get(Constants.SYS_CONFIG_KEY + "ops.health.probe-enabled")).thenReturn("__no_override__");

        List<ConfigItemVo> items = configService.listItems(null);

        ConfigItemVo captcha = items.stream()
                .filter(it -> "captcha.enabled".equals(it.getConfigKey())).findFirst().orElseThrow(AssertionError::new);
        Assert.assertTrue(captcha.getOverridden());
        Assert.assertEquals(ConfigSource.DB_OVERRIDE.name(), captcha.getSource());
        Assert.assertEquals("true", captcha.getEffectiveValue());

        ConfigItemVo sso = items.stream()
                .filter(it -> "sso.enabled".equals(it.getConfigKey())).findFirst().orElseThrow(AssertionError::new);
        Assert.assertFalse(sso.getOverridden());
        Assert.assertEquals(ConfigSource.ENVIRONMENT.name(), sso.getSource());
        Assert.assertEquals("false", sso.getEffectiveValue());
    }

    @Test
    public void listItems_filtersByGroup() {
        useEnvironment();
        when(redisUtil.get(anyString())).thenReturn("__no_override__");

        List<ConfigItemVo> opsItems = configService.listItems(ConfigKey.ConfigGroup.OPS.name());

        Assert.assertFalse(opsItems.isEmpty());
        opsItems.forEach(it ->
                Assert.assertEquals(ConfigKey.ConfigGroup.OPS.name(), it.getGroupCode()));
    }
}
