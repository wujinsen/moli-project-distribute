package com.moli.user.center.server.operation.service;

import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.dto.operation.OperationPortMatrixSaveRequest;
import com.moli.user.center.common.domain.entity.OperationPortMatrixInfo;
import com.moli.user.center.common.domain.vo.OperationPortMatrixVo;

public interface OperationPortMatrixService {

    PageRes<OperationPortMatrixVo> list(OperationPortMatrixInfo query);

    OperationPortMatrixVo getById(Long id);

    void create(OperationPortMatrixSaveRequest request);

    void update(OperationPortMatrixSaveRequest request);

    void deleteByIds(Long[] ids);
}
