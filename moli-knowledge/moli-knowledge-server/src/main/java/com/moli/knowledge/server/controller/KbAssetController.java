package com.moli.knowledge.server.controller;

import com.moli.knowledge.server.service.KbAssetService;
import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.KbWikiAssetUploadVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * T22 R0/R2：wiki / raw inline 图片只读 + F2 wiki 插图上传。
 */
@RestController
@RequestMapping("/kb")
@Api(tags = "知识库 Asset 资源")
public class KbAssetController {

    @Resource
    private KbAssetService kbAssetService;

    @GetMapping("/raw/asset")
    @ApiOperation("读取 raw 树下图片（过渡回迁 / D 档直链）；需空间 viewer")
    public void rawAsset(@ApiParam("相对 raw 根路径，如 wujinsen_markdown/.../imageFile1.png")
                         @RequestParam String path,
                         @RequestParam(required = false) Long spaceId,
                         HttpServletResponse response) {
        kbAssetService.serveRawAsset(spaceId, path, response);
    }

    @GetMapping({"/wiki/asset", "/wiki-moli/asset"})
    @ApiOperation("读取 wiki 页 {slug}.assets/ 下图片；需空间 viewer")
    public void wikiAsset(@ApiParam("wiki 全路径 slug") @RequestParam String slug,
                          @ApiParam("相对 asset 目录，如 imageFile1.png 或 assets/imageFile1.png")
                          @RequestParam String rel,
                          @RequestParam(required = false) Long spaceId,
                          HttpServletResponse response) {
        kbAssetService.serveWikiAsset(spaceId, slug, rel, response);
    }

    @PostMapping({"/wiki/asset", "/wiki-moli/asset"})
    @ApiOperation("上传 wiki 页 inline 插图至 {slug}.assets/；需空间 editor；wiki 文件须已存在")
    public MoliResult<KbWikiAssetUploadVo> uploadWikiAsset(
            @ApiParam("wiki 全路径 slug") @RequestParam String slug,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) Long spaceId) {
        return MoliResult.success(kbAssetService.uploadWikiAsset(spaceId, slug, file));
    }
}
