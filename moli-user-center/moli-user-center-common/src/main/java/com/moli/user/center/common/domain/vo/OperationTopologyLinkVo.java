package com.moli.user.center.common.domain.vo;

import lombok.Data;

@Data
public class OperationTopologyLinkVo {

    private String source;
    private String target;

    /** deploys | depends_on */
    private String type;
}
