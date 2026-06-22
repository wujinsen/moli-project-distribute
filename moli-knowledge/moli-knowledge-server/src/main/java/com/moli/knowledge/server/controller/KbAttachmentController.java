package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.entity.KbAttachment;
import com.moli.knowledge.server.service.KbAttachmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/kb/attachment")
@Api(tags = "文档附件")
public class KbAttachmentController {

    @Resource
    private KbAttachmentService kbAttachmentService;

    @GetMapping("/list")
    @ApiOperation("文档附件列表")
    public MoliResult<List<KbAttachment>> list(@RequestParam Long documentId) {
        return MoliResult.success(kbAttachmentService.listByDocument(documentId));
    }

    @PostMapping("/upload")
    @ApiOperation("上传附件")
    public MoliResult<KbAttachment> upload(@RequestParam Long documentId,
                                           @RequestParam("file") MultipartFile file) {
        return MoliResult.success(kbAttachmentService.upload(documentId, file));
    }

    @GetMapping("/{id}")
    @ApiOperation("下载附件")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        kbAttachmentService.download(id, response);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除附件")
    public MoliResult<Boolean> delete(@PathVariable Long id) {
        kbAttachmentService.delete(id);
        return MoliResult.success(Boolean.TRUE);
    }
}
