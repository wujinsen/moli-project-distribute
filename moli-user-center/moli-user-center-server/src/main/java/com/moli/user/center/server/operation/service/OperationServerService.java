package com.moli.user.center.server.operation.service;

import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.vo.OperationServerInfoVo;
import com.moli.user.center.common.domain.vo.OperationServerTopologyVo;
import com.moli.user.center.common.domain.vo.OperationServerVo;
import com.moli.common.page.PageRes;

public interface OperationServerService {

    PageRes<OperationServerVo> list(OperationServerInfoVo query);

    OperationServerVo getById(Long id);

    void create(OperationServerInfo form);

    void update(OperationServerInfo form);

    void deleteByIds(Long[] ids);

    OperationServerTopologyVo getTopology(Long id);

    OperationServerVo checkHealth(Long id);
}
