package com.moli.user.center.server.operation.service.impl;

import com.moli.user.center.common.domain.dto.operation.OperationProjectSaveRequest;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.service.OperationProjectLinkService;
import com.moli.user.center.server.operation.support.OperationServerBindingSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class OperationProjectServiceImplTest {

    @InjectMocks
    private OperationProjectServiceImpl projectService;

    @Mock
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Mock
    private OperationServerBindingSupport serverBindingSupport;
    @Mock
    private OperationProjectLinkService operationProjectLinkService;

    @Test
    public void create_returns_new_id_and_syncs_server_links() {
        OperationProjectSaveRequest request = new OperationProjectSaveRequest();
        request.setProjectName("moli-server");
        request.setServerIds(Arrays.asList(202L, 201L));

        doAnswer(invocation -> {
            OperationProjectDeployInfo row = invocation.getArgument(0);
            row.setId(401L);
            return 1;
        }).when(operationProjectDeployInfoMapper).insert(any(OperationProjectDeployInfo.class));

        Long id = projectService.create(request);

        assertEquals(Long.valueOf(401L), id);
        assertEquals(Long.valueOf(202L), request.getServerId());

        ArgumentCaptor<OperationProjectDeployInfo> rowCaptor = ArgumentCaptor.forClass(OperationProjectDeployInfo.class);
        verify(operationProjectDeployInfoMapper).insert(rowCaptor.capture());
        assertEquals(Long.valueOf(202L), rowCaptor.getValue().getServerId());

        verify(serverBindingSupport).bindProject(any(OperationProjectDeployInfo.class));
        verify(operationProjectLinkService).syncLinks(eq(401L), eq(Arrays.asList(202L, 201L)), eq(202L));
    }
}
