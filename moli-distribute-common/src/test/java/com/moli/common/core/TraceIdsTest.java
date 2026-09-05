package com.moli.common.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TraceIdsTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void rootId_stripsTidPrefixAndSegmentSuffix() {
        assertEquals(
                "13eef851b9434faa8dfcadbeeaa924a7",
                TraceIds.rootId("TID:13eef851b9434faa8dfcadbeeaa924a7.128.17882712296580005")
        );
        assertEquals(
                "13eef851b9434faa8dfcadbeeaa924a7",
                TraceIds.rootId("13eef851b9434faa8dfcadbeeaa924a7.128.1")
        );
        assertEquals(
                "13eef851b9434faa8dfcadbeeaa924a7",
                TraceIds.rootId("13EEF851B9434FAA8DFCADBEEAA924A7")
        );
    }

    @Test
    void rootId_rejectsMissingOrIgnored() {
        assertNull(TraceIds.rootId(null));
        assertNull(TraceIds.rootId(""));
        assertNull(TraceIds.rootId("N/A"));
        assertNull(TraceIds.rootId("Ignored_Trace"));
        assertNull(TraceIds.rootId("not-a-trace"));
    }

    @Test
    void currentRoot_readsMdcWhenToolkitAbsent() {
        assertNull(TraceIds.currentRoot());
        MDC.put("tid", "TID:13eef851b9434faa8dfcadbeeaa924a7.9.9");
        assertEquals("13eef851b9434faa8dfcadbeeaa924a7", TraceIds.currentRoot());
    }

    @Test
    void errorEnvelope_attachesRootTraceIdFromMdc() {
        MDC.put("trace_id", "13eef851b9434faa8dfcadbeeaa924a7.128.1");
        MoliResult<String> result = MoliResult.error("boom");
        assertEquals("13eef851b9434faa8dfcadbeeaa924a7", result.getTraceId());
        assertEquals("boom", result.getData());
    }

    @Test
    void errorEnvelope_omitsTraceIdWhenNoContext() {
        assertNull(MoliResult.errorMsg(500, "服务器错误").getTraceId());
    }
}
