package com.moli.user.center.server.operation.service;

import com.moli.user.center.common.domain.vo.OperationComponentLinksVo;

import java.util.List;

public interface OperationComponentLinkService {

    OperationComponentLinksVo getLinks(Long componentId);

    void saveLinks(Long componentId, OperationComponentLinksVo links);

    /** 保存组件时同步 N:N；serverIds 为空则仅保留主 serverId 关联 */
    void syncLinks(Long componentId, List<Long> serverIds, Long primaryServerId);
}
