package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.WikiGovernAiBatchFixRequest;
import com.moli.knowledge.server.dto.WikiGovernAiBatchFixResultVo;
import com.moli.knowledge.server.dto.WikiGovernAutoFixRequest;
import com.moli.knowledge.server.dto.WikiGovernAutoFixResultVo;
import com.moli.knowledge.server.dto.WikiGovernMergeHintRequest;
import com.moli.knowledge.server.dto.WikiGovernMergeHintResultVo;
import com.moli.knowledge.server.dto.WikiGovernOptionsVo;
import com.moli.knowledge.server.dto.WikiGovernScriptFixRequest;
import com.moli.knowledge.server.dto.WikiGovernScriptFixResultVo;

public interface KbWikiGovernService {

    WikiGovernOptionsVo getOptions();

    WikiGovernScriptFixResultVo scriptFix(WikiGovernScriptFixRequest request);

    WikiGovernAiBatchFixResultVo aiBatchFix(WikiGovernAiBatchFixRequest request);

    WikiGovernAutoFixResultVo autoFix(WikiGovernAutoFixRequest request);

    WikiGovernMergeHintResultVo mergeHint(WikiGovernMergeHintRequest request);
}
