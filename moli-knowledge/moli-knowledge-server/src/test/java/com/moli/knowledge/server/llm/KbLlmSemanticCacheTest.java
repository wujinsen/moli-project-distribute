package com.moli.knowledge.server.llm;

import com.alibaba.fastjson.JSON;
import com.moli.knowledge.server.config.KbLlmCacheProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbLlmSemanticCacheTest {

    @InjectMocks
    private KbLlmSemanticCache semanticCache;

    @Mock
    private KbLlmCacheProperties cacheProperties;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Before
    public void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(cacheProperties.isEnabled()).thenReturn(true);
        when(cacheProperties.getTtlSeconds()).thenReturn(3600);
        when(cacheProperties.isApproxEnabled()).thenReturn(false);
    }

    @Test
    public void lookup_returnsExactHit() {
        KbLlmSemanticCacheEntry entry = new KbLlmSemanticCacheEntry("ans", "glm", "glm-4", 1L);
        when(valueOperations.get(anyString())).thenReturn(JSON.toJSONString(entry));

        Optional<KbLlmSemanticCacheEntry> hit = semanticCache.lookup("ask", "glm-4", "sys", "question");

        Assert.assertTrue(hit.isPresent());
        Assert.assertEquals("ans", hit.get().getAnswer());
    }

    @Test
    public void put_writesRedisWithTtl() {
        semanticCache.put("ask", "glm-4", "sys", "question", "answer", "glm", "glm-4");

        verify(valueOperations).set(anyString(), anyString(), eq(3600L), eq(TimeUnit.SECONDS));
    }

    @Test
    public void lookup_disabledReturnsEmpty() {
        when(cacheProperties.isEnabled()).thenReturn(false);
        Assert.assertFalse(semanticCache.lookup("ask", "glm-4", "sys", "q").isPresent());
    }
}
