package com.moli.user.center.server.operation.deploy;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.dto.operation.OperationDeployServiceCatalog;
import com.moli.user.center.common.domain.dto.operation.OperationDeployServiceEntry;
import com.moli.user.center.server.operation.config.OperationDeployProperties;
import com.moli.user.center.server.operation.support.OperationBizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 部署 serviceKey 注册表：YAML {@code ops.deploy.services} 与项目别名映射的唯一入口。
 */
@Component
public class OperationDeployServiceRegistry {

    @Resource
    private OperationDeployProperties deployProperties;

    @PostConstruct
    public void init() {
        List<OperationDeployServiceEntry> configured = deployProperties.getServices();
        if (configured != null && !configured.isEmpty()) {
            OperationDeployServiceCatalog.install(configured);
        }
    }

    public Set<String> knownKeys() {
        return OperationDeployServiceCatalog.knownKeys();
    }

    public List<OperationDeployServiceEntry> entries() {
        return OperationDeployServiceCatalog.entries();
    }

    public boolean isKnownKey(String serviceKey) {
        return OperationDeployServiceCatalog.isKnownKey(serviceKey);
    }

    public String requireKnownKey(String serviceKey) {
        try {
            return OperationDeployServiceCatalog.requireKnownKey(serviceKey);
        } catch (IllegalArgumentException ex) {
            throw OperationBizException.params(ex.getMessage());
        }
    }

    public String normalizeServiceKey(String serviceKey) {
        if (StringUtils.isBlank(serviceKey)) {
            throw new BaseException("serviceKey 不能为空");
        }
        return requireKnownKey(serviceKey.trim().toLowerCase(Locale.ROOT));
    }

    public String resolveProjectName(String projectName) {
        return OperationDeployServiceCatalog.resolveProjectName(projectName);
    }
}
