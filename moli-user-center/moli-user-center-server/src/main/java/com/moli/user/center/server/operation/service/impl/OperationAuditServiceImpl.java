package com.moli.user.center.server.operation.service.impl;

import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.vo.OperationPortAuditItemVo;
import com.moli.user.center.common.domain.vo.OperationPortAuditVo;
import com.moli.user.center.common.domain.vo.OperationPortMatrixEntryVo;
import com.moli.user.center.server.operation.audit.OperationPortMatchStatus;
import com.moli.user.center.server.operation.audit.OperationPortMatrix;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.service.OperationAuditService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class OperationAuditServiceImpl implements OperationAuditService {

    @Resource
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Resource
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;

    @Override
    public OperationPortAuditVo auditPortMatrix() {
        List<OperationPortAuditItemVo> items = new ArrayList<>();
        for (OperationProjectDeployInfo row : operationProjectDeployInfoMapper.selectList(null)) {
            items.add(toItem("project", row.getId(), row.getProjectName(), row.getPort(), row.getEnvironment()));
        }
        for (OperationComponentDeployInfo row : operationComponentDeployInfoMapper.selectList(null)) {
            items.add(toItem("component", row.getId(), row.getComponentName(), row.getPort(), row.getEnvironment()));
        }

        OperationPortAuditVo vo = new OperationPortAuditVo();
        vo.setItems(items);
        vo.setTotal(items.size());
        int matched = 0;
        int mismatched = 0;
        int unmapped = 0;
        int skipped = 0;
        for (OperationPortAuditItemVo item : items) {
            switch (item.getPortMatchStatus()) {
                case OperationPortMatchStatus.MATCH:
                    matched++;
                    break;
                case OperationPortMatchStatus.MISMATCH:
                    mismatched++;
                    break;
                case OperationPortMatchStatus.SKIPPED:
                    skipped++;
                    break;
                default:
                    unmapped++;
                    break;
            }
        }
        vo.setMatched(matched);
        vo.setMismatched(mismatched);
        vo.setUnmapped(unmapped);
        vo.setSkipped(skipped);

        List<OperationPortMatrixEntryVo> matrix = new ArrayList<>();
        for (OperationPortMatrix.Entry entry : OperationPortMatrix.entries()) {
            OperationPortMatrixEntryVo row = new OperationPortMatrixEntryVo();
            row.setKey(entry.key);
            row.setExpectedPort(entry.expectedPort);
            row.setSource("docs/ops/production-checklist.md");
            matrix.add(row);
        }
        vo.setMatrix(matrix);
        return vo;
    }

    static OperationPortAuditItemVo toItem(String recordType, Long id, String name, String port, Integer environment) {
        OperationPortMatrix.PortCheck check = OperationPortMatrix.check(name, port);
        OperationPortAuditItemVo item = new OperationPortAuditItemVo();
        item.setId(id);
        item.setRecordType(recordType);
        item.setName(name);
        item.setActualPort(OperationPortMatrix.normalizePort(port));
        item.setExpectedPort(check.expectedPort);
        item.setMatrixKey(check.matrixKey);
        item.setPortMatchStatus(check.status);
        item.setMessage(check.message);
        item.setEnvironment(environment);
        return item;
    }
}
