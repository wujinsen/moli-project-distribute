package com.moli.ai.server.bi.controller;

import com.moli.ai.server.bi.constant.BiChatPermissionConstants;
import com.moli.ai.server.bi.dto.BiChatAskRequest;
import com.moli.ai.server.bi.dto.BiChatAskVo;
import com.moli.ai.server.bi.dto.BiChatTraceVo;
import com.moli.ai.server.bi.dto.BiSchemaTableVo;
import com.moli.ai.server.bi.service.BiChatService;
import com.moli.ai.server.bi.service.BiSchemaService;
import com.moli.common.core.MoliResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/bi/chat")
@Api(tags = "ChatBI")
public class BiChatController {

    private final BiChatService biChatService;
    private final BiSchemaService biSchemaService;

    public BiChatController(BiChatService biChatService, BiSchemaService biSchemaService) {
        this.biChatService = biChatService;
        this.biSchemaService = biSchemaService;
    }

    @PostMapping(value = "/ask", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
    @ApiOperation("自然语言问数（stream=true 返回 SSE）")
    @RequiresPermissions(BiChatPermissionConstants.QUERY)
    public Object ask(@RequestBody BiChatAskRequest request) {
        if (Boolean.TRUE.equals(request.getStream())) {
            return biChatService.askStream(request);
        }
        return biChatService.ask(request);
    }

    @GetMapping("/trace/{traceId}")
    @ApiOperation("问答决策链路")
    @RequiresPermissions(BiChatPermissionConstants.TRACE)
    public MoliResult<BiChatTraceVo> trace(@PathVariable("traceId") String traceId) {
        return biChatService.getTrace(traceId);
    }

    @GetMapping("/schema")
    @ApiOperation("可查询表白名单（脱敏）")
    @RequiresPermissions(BiChatPermissionConstants.QUERY)
    public MoliResult<List<BiSchemaTableVo>> schema() {
        return MoliResult.success(biSchemaService.listAllowedSchema());
    }
}
