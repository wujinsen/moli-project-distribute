package com.moli.user.center.server.operation.service;

import com.moli.user.center.common.domain.vo.OperationDeployStatusVo;

public interface OperationDeployService {

    OperationDeployStatusVo status(String serviceKey);

    OperationDeployStatusVo execute(String serviceKey, String action, String extraArg);
}
