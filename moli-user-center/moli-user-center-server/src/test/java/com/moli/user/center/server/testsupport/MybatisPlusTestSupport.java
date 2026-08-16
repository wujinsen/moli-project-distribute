package com.moli.user.center.server.testsupport;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationPortMatrixAliasInfo;
import com.moli.user.center.common.domain.entity.OperationPortMatrixInfo;
import com.moli.user.center.common.domain.entity.OperationPlatformInfo;
import com.moli.user.center.common.domain.entity.OperationProjectComponent;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationTask;
import com.moli.user.center.common.domain.entity.OperationServerComponent;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.entity.OperationServerProject;
import com.moli.user.center.common.domain.entity.SysConfig;
import com.moli.user.center.common.domain.entity.SysDept;
import com.moli.user.center.common.domain.entity.SysDictData;
import com.moli.user.center.common.domain.entity.SysDictType;
import com.moli.user.center.common.domain.entity.SysLoginLog;
import com.moli.user.center.common.domain.entity.SysMenu;
import com.moli.user.center.common.domain.entity.SysNotice;
import com.moli.user.center.common.domain.entity.SysNoticeReadCursor;
import com.moli.user.center.common.domain.entity.SysOperationLog;
import com.moli.user.center.common.domain.entity.SysPost;
import com.moli.user.center.common.domain.entity.SysRole;
import com.moli.user.center.common.domain.entity.SysRoleMenu;
import com.moli.user.center.common.domain.entity.SysSystem;
import com.moli.user.center.common.domain.entity.SysUser;
import com.moli.user.center.common.domain.entity.SysUserPost;
import com.moli.user.center.common.domain.entity.SysUserRole;
import com.moli.user.center.common.domain.entity.SysUserSystem;
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
                    SysUser.class,
                    SysSystem.class,
                    SysPost.class,
                    SysDept.class,
                    SysRole.class,
                    SysMenu.class,
                    SysDictType.class,
                    SysDictData.class,
                    SysLoginLog.class,
                    SysOperationLog.class,
                    SysUserRole.class,
                    SysUserPost.class,
                    SysUserSystem.class,
                    SysRoleMenu.class,
                    SysConfig.class,
                    SysNotice.class,
                    SysNoticeReadCursor.class,
                    OperationPlatformInfo.class,
                    OperationServerInfo.class,
                    OperationProjectDeployInfo.class,
                    OperationComponentDeployInfo.class,
                    OperationServerProject.class,
                    OperationServerComponent.class,
                    OperationProjectComponent.class,
                    OperationTask.class,
                    OperationPortMatrixInfo.class,
                    OperationPortMatrixAliasInfo.class
            };
            for (Class<?> entity : entities) {
                TableInfoHelper.initTableInfo(assistant, entity);
            }
            initialized = true;
        }
    }
}
