package com.moli.user.center.common.domain.dto.operation;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 部署中心可执行服务（moli-service.sh serviceKey + 项目名别名）。
 */
@Data
public class OperationDeployServiceEntry {

    /** moli-service.sh 参数，如 user-center / gateway */
    private String key;

    /** 展示名（可选） */
    private String label;

    /** 项目台账 projectName 别名，命中后映射到 key */
    private List<String> aliases = new ArrayList<>();
}
