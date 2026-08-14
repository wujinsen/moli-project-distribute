package com.moli.user.center.server.operation.service;

import com.moli.user.center.common.domain.dto.operation.OperationPlatformSaveRequest;
import com.moli.user.center.common.domain.entity.OperationPlatformInfo;
import com.moli.user.center.common.domain.vo.OperationPlatformVo;
import com.moli.user.center.common.domain.vo.OperationSecretRevealVo;
import com.moli.common.page.PageRes;

public interface OperationPlatformService {

    PageRes<OperationPlatformVo> list(OperationPlatformInfo query);

    OperationPlatformVo getById(Long id);

    void create(OperationPlatformSaveRequest request);

    void update(OperationPlatformSaveRequest request);

    void deleteByIds(Long[] ids);

    OperationSecretRevealVo revealPassword(Long id);
}
