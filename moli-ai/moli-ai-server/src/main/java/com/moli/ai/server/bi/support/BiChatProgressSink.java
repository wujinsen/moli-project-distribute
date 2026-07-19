package com.moli.ai.server.bi.support;

import com.moli.ai.server.bi.dto.BiChartVo;
import com.moli.ai.server.bi.dto.BiChatAskVo;

/**
 * ChatBI 问答进度回调；非流式传 null，SSE 传 {@link BiChatSseProgressSink}。
 */
public interface BiChatProgressSink {

    void stage(String stage, String traceId);

    void sql(String sql);

    void chart(BiChartVo chart);

    void token(String delta);

    void done(BiChatAskVo vo);

    void error(int code, String message);
}
