package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.user.center.common.domain.dto.operation.OperationServerTagsSupport;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationProjectComponent;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerComponent;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.entity.OperationServerProject;
import com.moli.user.center.common.domain.vo.OperationTopologyComponentNodeVo;
import com.moli.user.center.common.domain.vo.OperationTopologyGraphVo;
import com.moli.user.center.common.domain.vo.OperationTopologyLinkVo;
import com.moli.user.center.common.domain.vo.OperationTopologyProjectNodeVo;
import com.moli.user.center.common.domain.vo.OperationTopologyServerNodeVo;
import com.moli.user.center.server.operation.audit.OperationPortMatrixPortCheck;
import com.moli.user.center.server.operation.audit.OperationPortMatrixProvider;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectComponentLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerComponentLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.mapper.OperationServerProjectLinkMapper;
import com.moli.user.center.server.operation.service.OperationTopologyService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OperationTopologyServiceImpl implements OperationTopologyService {

    @Resource
    private OperationServerMapper operationServerMapper;
    @Resource
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Resource
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;
    @Resource
    private OperationServerProjectLinkMapper operationServerProjectLinkMapper;
    @Resource
    private OperationServerComponentLinkMapper operationServerComponentLinkMapper;
    @Resource
    private OperationProjectComponentLinkMapper operationProjectComponentLinkMapper;
    @Resource
    private OperationPortMatrixProvider portMatrixProvider;

    @Override
    public OperationTopologyGraphVo getGraph() {
        OperationTopologyGraphVo graph = new OperationTopologyGraphVo();

        List<OperationServerInfo> servers = operationServerMapper.selectList(new LambdaQueryWrapper<>());
        for (OperationServerInfo row : servers) {
            OperationTopologyServerNodeVo node = new OperationTopologyServerNodeVo();
            node.setId("s-" + row.getId());
            node.setServerId(row.getId());
            node.setServerName(row.getServerName());
            node.setIp(row.getIp());
            node.setInnerIp(row.getInnerIp());
            node.setEnvironment(row.getEnvironment());
            node.setServerRole(row.getServerRole());
            node.setTags(OperationServerTagsSupport.parse(row.getTags()));
            node.setStatus(row.getStatus());
            graph.getServers().add(node);
        }

        List<OperationProjectDeployInfo> projects = operationProjectDeployInfoMapper.selectList(new LambdaQueryWrapper<>());
        for (OperationProjectDeployInfo row : projects) {
            OperationTopologyProjectNodeVo node = new OperationTopologyProjectNodeVo();
            node.setId("p-" + row.getId());
            node.setProjectId(row.getId());
            node.setProjectName(row.getProjectName());
            node.setPort(row.getPort());
            node.setEnvironment(row.getEnvironment());
            node.setDeployRunning(row.getDeployRunning());
            OperationPortMatrixPortCheck check = portMatrixProvider.check(row.getProjectName(), row.getPort());
            node.setPortMatchStatus(check.status);
            graph.getProjects().add(node);
        }

        List<OperationComponentDeployInfo> components = operationComponentDeployInfoMapper.selectList(new LambdaQueryWrapper<>());
        for (OperationComponentDeployInfo row : components) {
            OperationTopologyComponentNodeVo node = new OperationTopologyComponentNodeVo();
            node.setId("c-" + row.getId());
            node.setComponentId(row.getId());
            node.setComponentName(row.getComponentName());
            node.setPort(row.getPort());
            node.setVersion(row.getVersion());
            node.setEnvironment(row.getEnvironment());
            node.setStatus(row.getStatus());
            OperationPortMatrixPortCheck check = portMatrixProvider.check(row.getComponentName(), row.getPort());
            node.setPortMatchStatus(check.status);
            graph.getComponents().add(node);
        }

        Set<String> linkKeys = new HashSet<>();
        for (OperationServerProject link : operationServerProjectLinkMapper.selectList(new LambdaQueryWrapper<>())) {
            addDeployLink(graph, linkKeys, link.getServerId(), link.getProjectId(), true);
        }
        for (OperationProjectDeployInfo row : projects) {
            addDeployLink(graph, linkKeys, row.getServerId(), row.getId(), false);
        }

        for (OperationServerComponent link : operationServerComponentLinkMapper.selectList(new LambdaQueryWrapper<>())) {
            addDeployLink(graph, linkKeys, link.getServerId(), null, link.getComponentId(), true);
        }
        for (OperationComponentDeployInfo row : components) {
            addDeployLink(graph, linkKeys, row.getServerId(), null, row.getId(), false);
        }

        for (OperationProjectComponent link : operationProjectComponentLinkMapper.selectList(new LambdaQueryWrapper<>())) {
            String key = "depends_on:p-" + link.getProjectId() + ":c-" + link.getComponentId();
            if (linkKeys.add(key)) {
                OperationTopologyLinkVo edge = new OperationTopologyLinkVo();
                edge.setSource("p-" + link.getProjectId());
                edge.setTarget("c-" + link.getComponentId());
                edge.setType("depends_on");
                graph.getLinks().add(edge);
            }
        }

        return graph;
    }

    private void addDeployLink(OperationTopologyGraphVo graph, Set<String> linkKeys,
                               Long serverId, Long projectId, boolean fromNn) {
        addDeployLink(graph, linkKeys, serverId, projectId, null, fromNn);
    }

    private void addDeployLink(OperationTopologyGraphVo graph, Set<String> linkKeys,
                               Long serverId, Long projectId, Long componentId, boolean fromNn) {
        if (serverId == null) {
            return;
        }
        String source = "s-" + serverId;
        String target;
        if (projectId != null) {
            target = "p-" + projectId;
        } else if (componentId != null) {
            target = "c-" + componentId;
        } else {
            return;
        }
        String key = "deploys:" + source + ":" + target;
        if (linkKeys.add(key)) {
            OperationTopologyLinkVo edge = new OperationTopologyLinkVo();
            edge.setSource(source);
            edge.setTarget(target);
            edge.setType("deploys");
            graph.getLinks().add(edge);
        }
    }
}
