package com.moli.knowledge.server.guard;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KbGroundingSelfCheckSupportTest {

    @Test
    public void parseSelfCheckJson_computesCoverage() {
        String raw = "{\"supported\":[\"A\",\"B\"],\"unsupported\":[\"C\"],\"missingInfo\":[\"关键词\"]}";
        KbGroundingSelfCheckSupport.GroundingCheckResult r =
                KbGroundingSelfCheckSupport.parseSelfCheckJson(raw);
        assertFalse(r.parseFailed);
        assertEquals(2.0 / 3.0, r.coverage, 0.001);
        assertEquals(1, r.missingInfo.size());
    }

    @Test
    public void computeCoverage_emptyStatements_returnsOne() {
        assertEquals(1.0,
                KbGroundingSelfCheckSupport.computeCoverage(new ArrayList<>(), new ArrayList<>()), 0.001);
    }

    @Test
    public void parseSelfCheckJson_invalidJson_marksParseFailed() {
        KbGroundingSelfCheckSupport.GroundingCheckResult r =
                KbGroundingSelfCheckSupport.parseSelfCheckJson("not json");
        assertTrue(r.parseFailed);
    }
}
