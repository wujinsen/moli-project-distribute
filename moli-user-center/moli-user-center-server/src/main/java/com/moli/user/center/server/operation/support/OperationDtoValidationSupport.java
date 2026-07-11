package com.moli.user.center.server.operation.support;

import com.moli.user.center.common.domain.dto.operation.OperationDeployTaskRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.annotation.Resource;
import java.util.Set;

/**
 * 将 path/query 参数组装为 DTO 并执行 Bean Validation。
 */
@Component
public class OperationDtoValidationSupport {

    @Resource
    private Validator validator;

    public <T> T validate(T target) {
        Set<ConstraintViolation<T>> violations = validator.validate(target);
        if (!violations.isEmpty()) {
            throw OperationBizException.params(violations.iterator().next().getMessage());
        }
        return target;
    }

    public OperationDeployTaskRequest deployTask(String serviceKey, String action, Long serverId, Long projectId) {
        OperationDeployTaskRequest req = new OperationDeployTaskRequest();
        req.setServiceKey(StringUtils.trimToNull(serviceKey));
        req.setAction(StringUtils.trimToNull(action));
        req.setServerId(serverId);
        req.setProjectId(projectId);
        return validate(req);
    }
}
