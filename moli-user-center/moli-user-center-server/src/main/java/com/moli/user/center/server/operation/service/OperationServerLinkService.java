package com.moli.user.center.server.operation.service;

import com.moli.user.center.common.domain.vo.OperationHealthProbeResultVo;
import com.moli.user.center.common.domain.vo.OperationServerLinksVo;

public interface OperationServerLinkService {

    OperationServerLinksVo getLinks(Long serverId);

    void saveLinks(Long serverId, OperationServerLinksVo links);
}
