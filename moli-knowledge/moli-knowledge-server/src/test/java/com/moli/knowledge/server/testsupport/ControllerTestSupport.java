package com.moli.knowledge.server.testsupport;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moli.common.core.MoliResult;
import org.junit.Assert;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public final class ControllerTestSupport {

    static {
        MybatisPlusTestSupport.initAll();
    }

    private ControllerTestSupport() {
    }

    public static void assertSuccess(MoliResult<?> result) {
        Assert.assertNotNull(result);
        Assert.assertEquals(200, result.getCode());
    }

    public static void stubInsert(BaseMapper<?> mapper) {
        when(mapper.insert(any())).thenReturn(1);
    }

    public static void stubUpdate(BaseMapper<?> mapper) {
        when(mapper.updateById(any())).thenReturn(1);
    }
}
