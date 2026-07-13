package com.moli.knowledge.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbSyncProperties;
import com.moli.knowledge.server.config.KbWikiProperties;
import com.moli.knowledge.server.dto.WikiLintIssueVo;
import com.moli.knowledge.server.dto.WikiSpaceLintRequest;
import com.moli.knowledge.server.dto.WikiSpaceLintVo;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbWikiLintService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class KbWikiLintServiceImpl implements KbWikiLintService {

    @Resource
    private KbWikiProperties wikiProperties;
    @Resource
    private KbSyncProperties syncProperties;
    @Resource
    private KbSpaceMapper kbSpaceMapper;
    @Resource
    private KbAclService kbAclService;

    @Override
    public WikiSpaceLintVo lintSpace(WikiSpaceLintRequest request) {
        if (request == null) {
            throw new BaseException("请求不能为空");
        }
        KbSpace space = resolveSpace(request.getSpaceId(), request.getSpaceCode());
        kbAclService.assertCanEdit(space.getId());

        String wikiDir = wikiProperties.getSpaceDirs().get(space.getSpaceCode());
        if (StringUtils.isBlank(wikiDir)) {
            throw new BaseException("空间未配置 wiki 目录映射: " + space.getSpaceCode());
        }

        Path script = resolveScriptPath();
        Path jsonOut = null;
        try {
            jsonOut = Files.createTempFile("kb-lint-", ".json");

            List<String> command = new ArrayList<>();
            command.add(syncProperties.getPython());
            command.add(script.toString());
            command.add("--wiki-dir");
            command.add(wikiDir);
            command.add("--json");
            command.add(jsonOut.toString());
            if (Boolean.TRUE.equals(request.getStrict())) {
                command.add("--strict");
            }

            String output;
            int exitCode;
            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);
                Process process = pb.start();

                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append('\n');
                    }
                }
                boolean finished = process.waitFor(wikiProperties.getLintTimeoutSeconds(), TimeUnit.SECONDS);
                if (!finished) {
                    process.destroyForcibly();
                    throw new BaseException("Lint 脚本超时（" + wikiProperties.getLintTimeoutSeconds() + "s）");
                }
                exitCode = process.exitValue();
                output = sb.toString();
            } catch (BaseException e) {
                throw e;
            } catch (Exception e) {
                log.error("[wiki-lint] run lint.py failed", e);
                throw new BaseException("执行 Lint 失败：" + e.getMessage());
            }

            return parseResult(space.getSpaceCode(), wikiDir, jsonOut, exitCode, output);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("[wiki-lint] lint space failed", e);
            throw new BaseException("空间体检失败：" + e.getMessage());
        } finally {
            if (jsonOut != null) {
                try {
                    Files.deleteIfExists(jsonOut);
                } catch (Exception ignore) {
                    // 临时文件清理失败不影响结果
                }
            }
        }
    }

    private WikiSpaceLintVo parseResult(String spaceCode, String wikiDir, Path jsonOut,
                                        int exitCode, String output) throws Exception {
        WikiSpaceLintVo vo = new WikiSpaceLintVo();
        vo.setSpaceCode(spaceCode);
        vo.setWikiDir(wikiDir);
        vo.setExitCode(exitCode);
        vo.setOutputTail(tail(output, 2000));

        if (!Files.exists(jsonOut)) {
            throw new BaseException("Lint 未产出报告（exit=" + exitCode + "）：" + tail(output, 500));
        }
        String content = new String(Files.readAllBytes(jsonOut), StandardCharsets.UTF_8);
        JSONObject root = JSON.parseObject(content);

        JSONObject stats = root.getJSONObject("stats");
        vo.setStats(stats == null ? null : stats.getInnerMap());

        List<WikiLintIssueVo> issues = new ArrayList<>();
        JSONArray arr = root.getJSONArray("issues");
        if (arr != null) {
            for (int i = 0; i < arr.size(); i++) {
                JSONObject o = arr.getJSONObject(i);
                WikiLintIssueVo issue = new WikiLintIssueVo();
                issue.setLevel(o.getString("level"));
                issue.setKind(o.getString("kind"));
                issue.setPage(o.getString("page"));
                issue.setDetail(o.getString("detail"));
                issue.setSuggest(o.getString("suggest"));
                issues.add(issue);
            }
        }
        vo.setIssues(issues);
        return vo;
    }

    private KbSpace resolveSpace(Long spaceId, String spaceCode) {
        if (spaceId != null) {
            KbSpace space = kbSpaceMapper.selectById(spaceId);
            if (space == null) {
                throw new BaseException("空间不存在");
            }
            return space;
        }
        String code = StringUtils.defaultIfBlank(spaceCode, syncProperties.getSpaceCode());
        KbSpace space = kbSpaceMapper.selectOne(new LambdaQueryWrapper<KbSpace>()
                .eq(KbSpace::getSpaceCode, code)
                .last("limit 1"));
        if (space == null) {
            throw new BaseException("空间不存在: " + code);
        }
        return space;
    }

    private Path resolveScriptPath() {
        return com.moli.knowledge.server.util.KbRepoPathUtil.resolveExisting(
                wikiProperties.getLintScriptPath(), "Lint 脚本");
    }

    private String tail(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) {
            return text;
        }
        return text.substring(text.length() - maxLen);
    }
}
