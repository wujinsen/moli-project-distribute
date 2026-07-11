package com.moli.user.center.server.api;

import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.dto.operation.OperationPortMatrixSaveRequest;
import com.moli.user.center.common.domain.entity.OperationPortMatrixInfo;
import com.moli.user.center.common.domain.vo.OperationPortMatrixVo;
import com.moli.user.center.server.operation.controller.OperationPortMatrixController;
import com.moli.user.center.server.operation.service.OperationPortMatrixService;
import com.moli.user.center.server.testsupport.AbstractApiTest;
import com.moli.user.center.server.testsupport.ControllerTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationPortMatrixControllersApiTest extends AbstractApiTest {

    @InjectMocks
    private OperationPortMatrixController portMatrixController;

    @Mock
    private OperationPortMatrixService operationPortMatrixService;

    @Test
    public void GET_operation_port_matrix_list() {
        OperationPortMatrixVo row = sampleVo();
        PageRes<OperationPortMatrixVo> page = new PageRes<>();
        page.setTotal(1);
        page.setPageNum(1);
        page.setPageSize(10);
        page.setList(Collections.singletonList(row));
        when(operationPortMatrixService.list(any())).thenReturn(page);

        OperationPortMatrixInfo query = new OperationPortMatrixInfo();
        query.setPageNum(1);
        query.setPageSize(10);
        query.setMatrixKey("user");
        query.setEnabled(Boolean.TRUE);

        ControllerTestSupport.assertSuccess(portMatrixController.list(query));
        verify(operationPortMatrixService).list(any(OperationPortMatrixInfo.class));
    }

    @Test
    public void GET_operation_port_matrix_id() {
        when(operationPortMatrixService.getById(501L)).thenReturn(sampleVo());
        ControllerTestSupport.assertSuccess(portMatrixController.selectOne(501L));
    }

    @Test
    public void POST_operation_port_matrix_insert() {
        doNothing().when(operationPortMatrixService).create(any());
        OperationPortMatrixSaveRequest request = new OperationPortMatrixSaveRequest();
        request.setMatrixKey("minio");
        request.setDisplayName("MinIO");
        request.setExpectedPort("9000");
        request.setAliases(Arrays.asList("minio", "minio-api"));
        request.setSortOrder(90);
        request.setEnabled(true);
        request.setRemark("对象存储");

        ControllerTestSupport.assertSuccess(portMatrixController.insert(request));
        verify(operationPortMatrixService).create(any(OperationPortMatrixSaveRequest.class));
    }

    @Test
    public void PUT_operation_port_matrix_update() {
        doNothing().when(operationPortMatrixService).update(any());
        OperationPortMatrixSaveRequest request = new OperationPortMatrixSaveRequest();
        request.setId(502L);
        request.setMatrixKey("user-center");
        request.setDisplayName("用户中心");
        request.setExpectedPort("9080");
        request.setAliases(Arrays.asList("moli-server", "user-center-server"));
        request.setEnabled(true);

        ControllerTestSupport.assertSuccess(portMatrixController.update(request));
        verify(operationPortMatrixService).update(any(OperationPortMatrixSaveRequest.class));
    }

    @Test
    public void DELETE_operation_port_matrix_ids() {
        doNothing().when(operationPortMatrixService).deleteByIds(any());
        ControllerTestSupport.assertSuccess(portMatrixController.remove(new Long[]{501L, 502L}));
        verify(operationPortMatrixService).deleteByIds(new Long[]{501L, 502L});
    }

    private static OperationPortMatrixVo sampleVo() {
        OperationPortMatrixVo vo = new OperationPortMatrixVo();
        vo.setId(502L);
        vo.setMatrixKey("user-center");
        vo.setDisplayName("用户中心");
        vo.setExpectedPort("8888");
        vo.setAliases(Arrays.asList("moli-user-center", "moli-server"));
        vo.setSortOrder(20);
        vo.setEnabled(true);
        vo.setSource("migration:java-default");
        vo.setUsingDefaults(false);
        return vo;
    }
}
