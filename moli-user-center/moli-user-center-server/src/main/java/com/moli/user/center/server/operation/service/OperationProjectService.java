package com.moli.user.center.server.operation.service;

import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.vo.OperationProjectVo;
import com.moli.common.page.PageRes;

public interface OperationProjectService {

    PageRes<OperationProjectVo> list(OperationProjectDeployInfo query);

    OperationProjectVo getById(Long id);

    void create(OperationProjectDeployInfo form);

    void update(OperationProjectDeployInfo form);

    void deleteByIds(Long[] ids);

    /** 按 serverIp 回填 serverId（台账与服务器表对齐） */
    void syncServerIdFromIp(OperationProjectDeployInfo row);
}
