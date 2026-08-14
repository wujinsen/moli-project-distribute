package com.moli.user.center.server.operation.support;

import com.moli.user.center.common.domain.dto.operation.OperationDeployTaskRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 将 path/query 参数组装为 DTO 并执行 Bean Validation。
 */
@Component
public class OperationDtoValidationSupport {

    private static final int BATCH_IDS_MAX = 50;

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

    /** 解析逗号分隔的 ID 列表（批量 links 等）。 */
    public List<Long> batchIds(String ids) {
        if (StringUtils.isBlank(ids)) {
            throw OperationBizException.params("ids 不能为空");
        }
        String[] parts = ids.split(",");
        if (parts.length > BATCH_IDS_MAX) {
            throw OperationBizException.params("ids 最多 " + BATCH_IDS_MAX + " 个");
        }
        List<Long> result = new ArrayList<>();
        for (String part : parts) {
            String trimmed = StringUtils.trimToEmpty(part);
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                result.add(Long.parseLong(trimmed));
            } catch (NumberFormatException e) {
                throw OperationBizException.params("ids 含非法数字: " + trimmed);
            }
        }
        if (result.isEmpty()) {
            throw OperationBizException.params("ids 不能为空");
        }
        return result;
    }
}
