package com.moli.user.center.server.operation.service.impl;

import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.vo.OperationDeployPresetsVo;
import com.moli.user.center.server.operation.config.OperationUploadProperties;
import com.moli.user.center.server.operation.service.OperationServerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationDeployPresetServiceImplTest {

    @InjectMocks
    private OperationDeployPresetServiceImpl presetService;

    @Mock
    private OperationServerService operationServerService;

    private final OperationUploadProperties uploadProperties = new OperationUploadProperties();

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(presetService, "uploadProperties", uploadProperties);
    }

    @Test
    public void getPresets_without_serverId_returns_defaults() {
        OperationDeployPresetsVo vo = presetService.getPresets(null);
        assertNotNull(vo.getPathPresets());
        assertFalse(vo.getPathPresets().isEmpty());
        assertNotNull(vo.getActionPresets());
        assertTrue(vo.getActionPresets().size() >= 6);
    }

    @Test
    public void getPresets_with_server_merges_upload_roots() {
        OperationServerInfo server = new OperationServerInfo();
        server.setId(204L);
        server.setUploadAllowedRoots("/home/ubuntu/custom/");
        when(operationServerService.requireEntity(204L)).thenReturn(server);

        OperationDeployPresetsVo vo = presetService.getPresets(204L);
        assertTrue(vo.getPathPresets().contains("/home/ubuntu/custom/"));
    }

    @Test
    public void getPresets_tolerates_null_upload_lists() {
        uploadProperties.setAllowedPaths(null);
        uploadProperties.setAllowAnyUnder(null);
        OperationDeployPresetsVo vo = presetService.getPresets(null);
        assertNotNull(vo.getPathPresets());
        assertFalse(vo.getPathPresets().isEmpty());
    }

    @Test
    public void getPresets_merges_configured_allowed_paths() {
        uploadProperties.setAllowedPaths(Arrays.asList("/data/moli/"));
        OperationDeployPresetsVo vo = presetService.getPresets(null);
        assertTrue(vo.getPathPresets().contains("/data/moli/"));
    }
}
