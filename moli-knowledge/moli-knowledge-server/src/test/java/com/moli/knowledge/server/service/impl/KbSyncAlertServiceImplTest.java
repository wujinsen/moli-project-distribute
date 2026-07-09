package com.moli.knowledge.server.service.impl;

import com.moli.knowledge.server.config.KbSyncProperties;
import com.moli.knowledge.server.dto.SyncTriggerVo;
import com.moli.knowledge.server.sync.SyncTriggerSource;
import org.junit.Assert;
import org.junit.Test;

public class KbSyncAlertServiceImplTest {

    @Test
    public void isFailure_detectsNonZeroExit() {
        SyncTriggerVo vo = new SyncTriggerVo();
        vo.setSuccess(false);
        vo.setExitCode(1);
        Assert.assertTrue(KbSyncAlertServiceImpl.isFailure(vo, null));
    }

    @Test
    public void isFailure_ignoresSuccess() {
        SyncTriggerVo vo = new SyncTriggerVo();
        vo.setSuccess(true);
        vo.setExitCode(0);
        Assert.assertFalse(KbSyncAlertServiceImpl.isFailure(vo, null));
    }

    @Test
    public void isFailure_detectsException() {
        Assert.assertTrue(KbSyncAlertServiceImpl.isFailure(null,
                new RuntimeException("timeout")));
    }

    @Test
    public void buildFeishuPayload_hasTextMsgType() throws Exception {
        String json = KbSyncAlertServiceImpl.buildFeishuPayload("hello");
        Assert.assertTrue(json.contains("\"msg_type\":\"text\""));
        Assert.assertTrue(json.contains("hello"));
    }

    @Test
    public void buildWeComPayload_hasTextMsgType() throws Exception {
        String json = KbSyncAlertServiceImpl.buildWeComPayload("hello");
        Assert.assertTrue(json.contains("\"msgtype\":\"text\""));
        Assert.assertTrue(json.contains("hello"));
    }

    @Test
    public void buildAlertText_includesSpaceAndSource() {
        KbSyncProperties.Alert alert = new KbSyncProperties.Alert();
        alert.setIncludeOutputTail(false);
        SyncTriggerVo vo = new SyncTriggerVo();
        vo.setExitCode(1);
        String text = KbSyncAlertServiceImpl.buildAlertText(
                SyncTriggerSource.SCHEDULED, "enterprise-kb", vo, null, alert);
        Assert.assertTrue(text.contains("enterprise-kb"));
        Assert.assertTrue(text.contains("定时任务"));
        Assert.assertTrue(text.contains("exitCode: 1"));
    }
}
