package com.moli.knowledge.server.guard;

import com.moli.knowledge.server.config.KbGuardrailsProperties;
import com.moli.knowledge.server.dto.AskResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbOutputGroundingServiceTest {

    @Mock
    private KbGuardrailsProperties guardrailsProperties;
    @Mock
    private KbGroundingSelfCheckSupport groundingSelfCheckSupport;
    @Mock
    private KbGuardrailsProperties.Grounding groundingConfig;

    @InjectMocks
    private KbOutputGroundingService service;

    @Before
    public void setUp() {
        when(guardrailsProperties.getGrounding()).thenReturn(groundingConfig);
        when(groundingConfig.getLowThreshold()).thenReturn(0.8);
    }

    @Test
    public void applyGrounding_disabled_skipsCheck() {
        when(guardrailsProperties.isEnabled()).thenReturn(false);
        AskResponse resp = generativeResponse("answer");
        service.applyGrounding(resp, 1L, InputGuardOutcome.bypass("q"));
        verify(groundingSelfCheckSupport, never()).check(any(), any(), any());
        assertNull(resp.getGuard());
    }

    @Test
    public void applyGrounding_enabled_setsLowConfidence() {
        when(guardrailsProperties.isEnabled()).thenReturn(true);
        when(groundingConfig.isEnabled()).thenReturn(true);
        KbGroundingSelfCheckSupport.GroundingCheckResult check =
                new KbGroundingSelfCheckSupport.GroundingCheckResult();
        check.coverage = 0.5;
        check.unsupported = Arrays.asList("无据陈述");
        when(groundingSelfCheckSupport.check(any(), eq(1L), any())).thenReturn(check);

        AskResponse resp = generativeResponse("answer");
        service.applyGrounding(resp, 1L, InputGuardOutcome.pass("q", Collections.emptyList(), false));
        assertNotNull(resp.getGuard());
        assertTrue(resp.getGuard().getGroundingApplied());
        assertTrue(resp.getGuard().getGroundingLow());
        assertEquals(0.5, resp.getGuard().getCoverage(), 0.001);
        assertEquals(1, resp.getGuard().getUnsupportedStatements().size());
    }

    @Test
    public void mergeAgenticGuard_disabled_returnsNullWhenBypassed() {
        when(guardrailsProperties.isEnabled()).thenReturn(false);
        assertNull(service.mergeAgenticGuard(InputGuardOutcome.bypass("q"), true, 0.9,
                Collections.singletonList("x")));
    }

    @Test
    public void mergeAgenticGuard_enabled_mapsCoverageWithoutSecondCheck() {
        when(guardrailsProperties.isEnabled()).thenReturn(true);
        AskGuardVo guard = service.mergeAgenticGuard(
                InputGuardOutcome.pass("q", Collections.emptyList(), false),
                true, 0.75, Arrays.asList("unsupported A"));
        assertNotNull(guard);
        assertTrue(guard.getGroundingApplied());
        assertTrue(guard.getGroundingLow());
        assertEquals(0.75, guard.getCoverage(), 0.001);
        assertEquals(1, guard.getUnsupportedStatements().size());
    }

    @Test
    public void applyGrounding_retrievalMode_skips() {
        when(guardrailsProperties.isEnabled()).thenReturn(true);
        when(groundingConfig.isEnabled()).thenReturn(true);
        AskResponse resp = new AskResponse();
        resp.setMode("retrieval");
        resp.setAnswer("retrieval");
        service.applyGrounding(resp, 1L, InputGuardOutcome.bypass("q"));
        verify(groundingSelfCheckSupport, never()).check(any(), any(), any());
        assertNull(resp.getGuard());
    }

    private static AskResponse generativeResponse(String answer) {
        AskResponse resp = new AskResponse();
        resp.setMode("generative");
        resp.setAnswer(answer);
        resp.setCitations(Collections.emptyList());
        return resp;
    }
}
