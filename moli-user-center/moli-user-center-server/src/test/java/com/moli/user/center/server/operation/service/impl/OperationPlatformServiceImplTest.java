package com.moli.user.center.server.operation.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.dto.operation.OperationPlatformSaveRequest;
import com.moli.user.center.common.domain.entity.OperationPlatformInfo;
import com.moli.user.center.server.operation.mapper.OperationPlatformMapper;
import com.moli.user.center.server.operation.support.OperationBizException;
import com.moli.user.center.server.operation.support.OperationCrudSupport;
import com.moli.user.center.server.operation.support.OperationSecretCrudSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationPlatformServiceImplTest {

    @InjectMocks
    private OperationPlatformServiceImpl platformService;

    @Spy
    private OperationCrudSupport crudSupport = new OperationCrudSupport();

    @Mock
    private OperationPlatformMapper operationPlatformMapper;

    @Mock
    private OperationSecretCrudSupport secretCrudSupport;

    @Test
    public void update_persists_changed_fields_and_preserves_create_audit() {
        OperationPlatformInfo existing = new OperationPlatformInfo();
        existing.setId(101L);
        existing.setPlatformName("AWS");
        existing.setCreateId(1L);
        existing.setCreateTime(new Date(1_000L));
        existing.setPassword("cipher");
        when(operationPlatformMapper.selectById(101L)).thenReturn(existing);
        when(secretCrudSupport.mergeOnUpdate(null, "cipher")).thenReturn("cipher");
        when(operationPlatformMapper.updateById(any())).thenReturn(1);

        OperationPlatformSaveRequest request = new OperationPlatformSaveRequest();
        request.setId(101L);
        request.setPlatformName("AWS Console");
        request.setUrl("https://console.aws.amazon.com");
        request.setAccount("ops@example.com");
        request.setEnvironment(4);
        request.setRemark("updated");

        platformService.update(request);

        ArgumentCaptor<OperationPlatformInfo> captor = ArgumentCaptor.forClass(OperationPlatformInfo.class);
        verify(operationPlatformMapper).updateById(captor.capture());
        OperationPlatformInfo saved = captor.getValue();
        assertEquals(Long.valueOf(101L), saved.getId());
        assertEquals("AWS Console", saved.getPlatformName());
        assertEquals("https://console.aws.amazon.com", saved.getUrl());
        assertEquals("ops@example.com", saved.getAccount());
        assertEquals(Integer.valueOf(4), saved.getEnvironment());
        assertEquals("updated", saved.getRemark());
        assertEquals("cipher", saved.getPassword());
        assertEquals(Long.valueOf(1L), saved.getCreateId());
        assertEquals(existing.getCreateTime(), saved.getCreateTime());
    }

    @Test
    public void update_fails_when_no_row_updated() {
        OperationPlatformInfo existing = new OperationPlatformInfo();
        existing.setId(101L);
        existing.setPlatformName("AWS");
        when(operationPlatformMapper.selectById(101L)).thenReturn(existing);
        when(secretCrudSupport.mergeOnUpdate(null, null)).thenReturn(null);
        when(operationPlatformMapper.updateById(any())).thenReturn(0);

        OperationPlatformSaveRequest request = new OperationPlatformSaveRequest();
        request.setId(101L);
        request.setPlatformName("AWS Console");

        try {
            platformService.update(request);
            fail("expected not found when updateById affects 0 rows");
        } catch (BaseException ex) {
            assertEquals(Integer.valueOf(OperationBizException.CODE_ENTITY_NOT_FOUND), ex.getErrorCode());
        }
    }
}
