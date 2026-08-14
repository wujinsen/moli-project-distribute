package com.moli.knowledge.server.exception;

import com.moli.common.core.MoliResult;
import com.moli.common.enums.ResponseCodeEnums;
import com.moli.knowledge.server.dto.IngestRawConflictVo;
import com.moli.knowledge.server.dto.RawCoverageItemVo;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collections;

public class KbKnowledgeExceptionHandlerTest {

    @Test
    public void ingestRawConflictHandler_returnsStructuredData() {
        IngestRawConflictVo detail = new IngestRawConflictVo();
        detail.setSpaceId(1L);
        detail.setJobId(2L);
        RawCoverageItemVo item = new RawCoverageItemVo();
        item.setPath("fe/foo.md");
        item.setCoverage("covered");
        item.setMatchKind("exact");
        item.setWikiSlugs(Collections.singletonList("guides/x"));
        detail.setConflicts(Collections.singletonList(item));

        String msg = "raw 已被 wiki 引用…";
        IngestRawConflictException ex = new IngestRawConflictException(msg, detail);

        KbKnowledgeExceptionHandler handler = new KbKnowledgeExceptionHandler();
        MoliResult<IngestRawConflictVo> result = handler.ingestRawConflictHandler(new MockHttpServletRequest(), ex);

        Assert.assertEquals(ResponseCodeEnums.BIZ_ERROR_CODE.getCode().intValue(), result.getCode());
        Assert.assertEquals(msg, result.getMsg());
        Assert.assertNotNull(result.getData());
        Assert.assertEquals(IngestRawConflictVo.ERROR_KIND, result.getData().getErrorKind());
        Assert.assertEquals(Long.valueOf(1L), result.getData().getSpaceId());
        Assert.assertEquals(1, result.getData().getConflicts().size());
        Assert.assertEquals("fe/foo.md", result.getData().getConflicts().get(0).getPath());
    }
}
