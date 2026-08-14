package com.moli.user.center.server.operation.support;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * MyBatis BATCH 模式批量 updateById，减少探活等场景的 DB 往返。
 */
@Component
public class OperationMapperBatchSupport {

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public <T> void updateBatchById(Class<? extends BaseMapper<T>> mapperClass, List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            BaseMapper<T> mapper = session.getMapper(mapperClass);
            for (T entity : entities) {
                mapper.updateById(entity);
            }
            session.commit();
        }
    }
}
