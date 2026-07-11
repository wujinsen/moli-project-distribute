package com.moli.user.center.server.operation.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.dto.operation.OperationPortMatrixSaveRequest;
import com.moli.user.center.common.domain.entity.OperationPortMatrixInfo;
import com.moli.user.center.server.operation.audit.OperationPortMatrixProvider;
import com.moli.user.center.server.operation.mapper.OperationPortMatrixAliasMapper;
import com.moli.user.center.server.operation.mapper.OperationPortMatrixMapper;
import com.moli.user.center.server.operation.support.OperationCrudSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationPortMatrixServiceImplTest {

    @InjectMocks
    private OperationPortMatrixServiceImpl portMatrixService;

    @Spy
    private OperationCrudSupport crudSupport = new OperationCrudSupport();

    @Mock
    private OperationPortMatrixMapper operationPortMatrixMapper;
    @Mock
    private OperationPortMatrixAliasMapper operationPortMatrixAliasMapper;
    @Mock
    private OperationPortMatrixProvider portMatrixProvider;

    @Test
    public void create_rejects_duplicate_matrix_key() {
        when(operationPortMatrixMapper.selectCount(any())).thenReturn(1);

        OperationPortMatrixSaveRequest request = new OperationPortMatrixSaveRequest();
        request.setMatrixKey("user-center");
        request.setExpectedPort("8888");

        try {
            portMatrixService.create(request);
            fail("expected duplicate matrixKey");
        } catch (BaseException ignored) {
        }
    }

    @Test
    public void create_rejects_invalid_port() {
        when(operationPortMatrixMapper.selectCount(any())).thenReturn(0);
        when(operationPortMatrixMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(operationPortMatrixAliasMapper.selectList(any())).thenReturn(Collections.emptyList());

        OperationPortMatrixSaveRequest request = new OperationPortMatrixSaveRequest();
        request.setMatrixKey("minio");
        request.setExpectedPort("abc");

        try {
            portMatrixService.create(request);
            fail("expected invalid port");
        } catch (BaseException ignored) {
        }
    }

    @Test
    public void create_inserts_and_refreshes_provider() {
        when(operationPortMatrixMapper.selectCount(any())).thenReturn(0);
        when(operationPortMatrixMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(operationPortMatrixAliasMapper.selectList(any())).thenReturn(Collections.emptyList());

        OperationPortMatrixSaveRequest request = new OperationPortMatrixSaveRequest();
        request.setMatrixKey("minio");
        request.setExpectedPort("9000");
        request.setAliases(Arrays.asList("minio-api", "minio"));

        portMatrixService.create(request);

        verify(operationPortMatrixMapper).insert(any(OperationPortMatrixInfo.class));
        verify(portMatrixProvider).refresh();
    }

    @Test
    public void update_rejects_matrix_key_rename() {
        OperationPortMatrixInfo existing = new OperationPortMatrixInfo();
        existing.setId(501L);
        existing.setMatrixKey("gateway");
        when(operationPortMatrixMapper.selectById(501L)).thenReturn(existing);

        OperationPortMatrixSaveRequest request = new OperationPortMatrixSaveRequest();
        request.setId(501L);
        request.setMatrixKey("moli-gateway");
        request.setExpectedPort("21000");

        try {
            portMatrixService.update(request);
            fail("expected matrixKey immutable");
        } catch (BaseException ignored) {
        }
    }
}
