package com.moli.user.center.server.operation.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.exception.BaseException;
import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.entity.OperationPlatformInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationCrudSupportTest {

    @InjectMocks
    private OperationCrudSupport crudSupport;

    @Mock
    private BaseMapper<OperationPlatformInfo> mapper;

    @Test
    public void selectPage_maps_records_and_totals() {
        OperationPlatformInfo row = new OperationPlatformInfo();
        row.setId(1L);
        row.setPlatformName("Aliyun");

        when(mapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Page<OperationPlatformInfo> p = invocation.getArgument(0);
            p.setTotal(1);
            p.setRecords(Collections.singletonList(row));
            return p;
        });

        PageRes<String> result = crudSupport.selectPage(mapper, new LambdaQueryWrapper<>(),
                1, 10, r -> "vo-" + r.getPlatformName());

        assertEquals(Integer.valueOf(1), result.getTotal());
        assertEquals(Integer.valueOf(1), result.getPageNum());
        assertEquals(Integer.valueOf(10), result.getPageSize());
        assertEquals(Collections.singletonList("vo-Aliyun"), result.getList());
    }

    @Test
    public void requireRow_returns_row_when_present() {
        OperationPlatformInfo row = new OperationPlatformInfo();
        row.setId(9L);
        when(mapper.selectById(9L)).thenReturn(row);

        assertSame(row, crudSupport.requireRow(mapper, 9L, "平台"));
    }

    @Test
    public void requireRow_throws_not_found_when_missing() {
        when(mapper.selectById(99L)).thenReturn(null);

        try {
            crudSupport.requireRow(mapper, 99L, "平台");
            fail("expected not found");
        } catch (BaseException ex) {
            assertEquals(Integer.valueOf(OperationBizException.CODE_ENTITY_NOT_FOUND), ex.getErrorCode());
        }
    }

    @Test
    public void assertUpdateId_rejects_null() {
        try {
            crudSupport.assertUpdateId(null);
            fail("expected missing id");
        } catch (BaseException ex) {
            assertEquals(Integer.valueOf(OperationBizException.CODE_MISSING_ID), ex.getErrorCode());
        }
    }

    @Test
    public void deleteEach_runs_before_and_delete_for_each_id() {
        AtomicInteger beforeCount = new AtomicInteger();
        AtomicInteger deleteCount = new AtomicInteger();

        crudSupport.deleteEach(new Long[]{1L, 2L}, id -> beforeCount.incrementAndGet(), id -> deleteCount.incrementAndGet());

        assertEquals(2, beforeCount.get());
        assertEquals(2, deleteCount.get());
    }

    @Test
    public void assertRowsUpdated_rejects_zero_rows() {
        try {
            crudSupport.assertRowsUpdated(0, "平台", 101L);
            fail("expected not found");
        } catch (BaseException ex) {
            assertEquals(Integer.valueOf(OperationBizException.CODE_ENTITY_NOT_FOUND), ex.getErrorCode());
        }
    }

    @Test
    public void deleteEach_skips_when_ids_null() {
        crudSupport.deleteEach(null, id -> fail("should not run"), id -> fail("should not run"));
    }

    @Test
    public void deleteEach_without_before_only_deletes() {
        AtomicInteger deleteCount = new AtomicInteger();

        crudSupport.deleteEach(new Long[]{3L}, id -> {
            assertEquals(Long.valueOf(3L), id);
            deleteCount.incrementAndGet();
        });

        assertEquals(1, deleteCount.get());
    }
}
