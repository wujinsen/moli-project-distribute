package com.moli.knowledge.server.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KbResearchPropertiesTest {

    @Test
    public void normalizedMaxSections_capsAt10() {
        KbResearchProperties props = new KbResearchProperties();
        assertEquals(10, props.normalizedMaxSections(99));
        assertEquals(6, props.normalizedMaxSections(null));
    }

    @Test
    public void normalizedMaxRetrieveRounds_capsAt3() {
        KbResearchProperties props = new KbResearchProperties();
        assertEquals(3, props.normalizedMaxRetrieveRounds(9));
    }
}
