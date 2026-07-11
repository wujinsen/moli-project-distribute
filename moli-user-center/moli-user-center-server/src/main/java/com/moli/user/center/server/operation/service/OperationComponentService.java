package com.moli.user.center.server.operation.service;

import com.moli.user.center.common.domain.dto.operation.OperationComponentSaveRequest;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.vo.OperationComponentVo;
import com.moli.user.center.common.domain.vo.OperationSecretRevealVo;
import com.moli.common.page.PageRes;

public interface OperationComponentService {

    PageRes<OperationComponentVo> list(OperationComponentDeployInfo query);

    OperationComponentVo getById(Long id);

    void create(OperationComponentSaveRequest request);

    void update(OperationComponentSaveRequest request);

    void deleteByIds(Long[] ids);

    OperationSecretRevealVo revealPassword(Long id);

    OperationComponentVo checkHealth(Long id);
}
