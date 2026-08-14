package com.moli.user.center.server.operation.service;

import com.moli.user.center.common.domain.vo.OperationProjectComponentLinksVo;

import java.util.List;

public interface OperationProjectComponentLinkService {

    OperationProjectComponentLinksVo getLinks(Long projectId);

    void saveLinks(Long projectId, OperationProjectComponentLinksVo links);

    void syncLinks(Long projectId, List<Long> componentIds);
}
