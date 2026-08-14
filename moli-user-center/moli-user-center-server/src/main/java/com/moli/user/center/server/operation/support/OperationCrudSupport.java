package com.moli.user.center.server.operation.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.page.PageRes;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 台账 CRUD 公共分页 / 查单 / 删除循环（Phase R4）。
 */
@Component
public class OperationCrudSupport {

    public <E, V> PageRes<V> selectPage(BaseMapper<E> mapper, LambdaQueryWrapper<E> wrapper,
                                          int pageNum, int pageSize, Function<E, V> toVo) {
        Page<E> page = new Page<>();
        page.setCurrent(pageNum);
        page.setSize(pageSize);
        mapper.selectPage(page, wrapper);

        PageRes<V> result = new PageRes<>();
        result.setTotal((int) page.getTotal());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setList(page.getRecords().stream().map(toVo).collect(Collectors.toList()));
        return result;
    }

    public <E> E requireRow(BaseMapper<E> mapper, Long id, String entityLabel) {
        E row = mapper.selectById(id);
        if (row == null) {
            throw OperationBizException.notFound(entityLabel, id);
        }
        return row;
    }

    public void assertUpdateId(Long id) {
        if (id == null) {
            throw OperationBizException.missingId();
        }
    }

    public void assertRowsUpdated(int rows, String entityLabel, Long id) {
        if (rows <= 0) {
            throw OperationBizException.notFound(entityLabel, id);
        }
    }

    public void deleteEach(Long[] ids, Consumer<Long> beforeDelete, Consumer<Long> deleteAction) {
        if (ids == null) {
            return;
        }
        for (Long id : ids) {
            if (beforeDelete != null) {
                beforeDelete.accept(id);
            }
            deleteAction.accept(id);
        }
    }

    public void deleteEach(Long[] ids, Consumer<Long> deleteAction) {
        deleteEach(ids, null, deleteAction);
    }
}
