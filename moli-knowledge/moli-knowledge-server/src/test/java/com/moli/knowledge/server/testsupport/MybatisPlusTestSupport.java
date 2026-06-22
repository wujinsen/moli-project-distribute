package com.moli.knowledge.server.testsupport;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.moli.knowledge.server.entity.KbAttachment;
import com.moli.knowledge.server.entity.KbDocument;
import org.apache.ibatis.builder.MapperBuilderAssistant;

public final class MybatisPlusTestSupport {

    private static volatile boolean initialized;

    private MybatisPlusTestSupport() {
    }

    public static void initAll() {
        if (initialized) {
            return;
        }
        synchronized (MybatisPlusTestSupport.class) {
            if (initialized) {
                return;
            }
            MybatisConfiguration configuration = new MybatisConfiguration();
            MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
            Class<?>[] entities = new Class<?>[]{
                    KbDocument.class,
                    KbAttachment.class
            };
            for (Class<?> entity : entities) {
                TableInfoHelper.initTableInfo(assistant, entity);
            }
            initialized = true;
        }
    }
}
