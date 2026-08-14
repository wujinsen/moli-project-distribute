package com.moli.user.center.server.operation.service;

import com.moli.user.center.common.domain.vo.OperationProjectLinksVo;

import java.util.List;

public interface OperationProjectLinkService {

    OperationProjectLinksVo getLinks(Long projectId);

    List<OperationProjectLinksVo> getLinksBatch(List<Long> projectIds);

    void saveLinks(Long projectId, OperationProjectLinksVo links);

    /** 保存项目时同步 N:N；serverIds 为空则仅保留主 serverId 关联 */
    void syncLinks(Long projectId, List<Long> serverIds, Long primaryServerId);
}
