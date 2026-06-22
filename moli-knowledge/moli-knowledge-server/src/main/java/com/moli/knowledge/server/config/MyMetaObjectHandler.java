package com.moli.knowledge.server.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.moli.knowledge.server.util.ShiroUtils;
import com.moli.user.center.common.domain.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "isDelete", () -> 0, Integer.class);
        this.strictInsertFill(metaObject, "createTime", () -> new Date(), Date.class);
        SysUser current = ShiroUtils.getUserInfo();
        if (current != null) {
            this.strictInsertFill(metaObject, "createId", () -> current.getId(), Long.class);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", () -> new Date(), Date.class);
        SysUser current = ShiroUtils.getUserInfo();
        if (current != null) {
            this.strictUpdateFill(metaObject, "updateId", () -> current.getId(), Long.class);
        }
    }
}
