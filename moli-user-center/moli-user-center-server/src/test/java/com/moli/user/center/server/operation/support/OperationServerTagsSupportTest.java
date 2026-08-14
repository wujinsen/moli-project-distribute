package com.moli.user.center.server.operation.support;

import com.moli.user.center.common.domain.dto.operation.OperationServerTagsSupport;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OperationServerTagsSupportTest {

    @Test
    public void normalizeList_dedupes_and_lowercases() {
        List<String> out = OperationServerTagsSupport.normalizeList(Arrays.asList("GZ", "gz", " Knowledge "));
        assertEquals(Arrays.asList("gz", "knowledge"), out);
    }

    @Test
    public void toJson_and_parse_roundtrip() {
        String json = OperationServerTagsSupport.toJson(Arrays.asList("pro", "gz"));
        assertEquals("[\"pro\",\"gz\"]", json);
        assertEquals(Arrays.asList("pro", "gz"), OperationServerTagsSupport.parse(json));
    }

    @Test
    public void isValidTag_rejects_invalid_chars() {
        assertTrue(OperationServerTagsSupport.isValidTag("gz"));
        assertFalse(OperationServerTagsSupport.isValidTag("bad tag"));
        assertFalse(OperationServerTagsSupport.isValidTag(""));
    }

    @Test
    public void mergeDistinct_sorts() {
        List<String> merged = OperationServerTagsSupport.mergeDistinct(Arrays.asList(
                Arrays.asList("pro", "gz"),
                Arrays.asList("dev", "gz")
        ));
        assertEquals(Arrays.asList("dev", "gz", "pro"), merged);
    }

    @Test
    public void parse_blank_returns_empty() {
        assertEquals(Collections.emptyList(), OperationServerTagsSupport.parse(null));
        assertEquals(Collections.emptyList(), OperationServerTagsSupport.parse("[]"));
    }
}
