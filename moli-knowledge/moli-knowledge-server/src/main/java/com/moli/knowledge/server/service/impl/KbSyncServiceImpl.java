package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbSyncProperties;
import com.moli.knowledge.server.config.KbWikiProperties;
import com.moli.knowledge.server.dto.SyncStatusVo;
import com.moli.knowledge.server.dto.SyncTriggerVo;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.entity.KbSyncLog;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.mapper.KbSyncLogMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbSyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class KbSyncServiceImpl implements KbSyncService {

    private static final Pattern JDBC_MYSQL = Pattern.compile(
            "jdbc:mysql://([^:/]+)(?::(\\d+))?/([^?]+)");

    @Resource
    private KbSyncLogMapper kbSyncLogMapper;
    @Resource
    private KbSpaceMapper kbSpaceMapper;
    @Resource
    private KbAclService kbAclService;
    @Resource
    private KbSyncProperties syncProperties;
    @Resource
    private KbWikiProperties wikiProperties;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    @Value("${spring.datasource.username}")
    private String dbUser;
    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Override
    public Page<KbSyncLog> logs(Long spaceId, String batchNo, int pageNum, int pageSize) {
        assertSyncPermission(spaceId);
        LambdaQueryWrapper<KbSyncLog> wrapper = new LambdaQueryWrapper<>();
        if (spaceId != null) {
            wrapper.eq(KbSyncLog::getSpaceId, spaceId);
        }
        if (StringUtils.isNotBlank(batchNo)) {
            wrapper.eq(KbSyncLog::getBatchNo, batchNo);
        }
        wrapper.orderByDesc(KbSyncLog::getCreateTime);
        return kbSyncLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public SyncStatusVo status(Long spaceId) {
        assertSyncPermission(spaceId);
        LambdaQueryWrapper<KbSyncLog> latestWrapper = new LambdaQueryWrapper<>();
        if (spaceId != null) {
            latestWrapper.eq(KbSyncLog::getSpaceId, spaceId);
        }
        latestWrapper.orderByDesc(KbSyncLog::getCreateTime).last("limit 1");
        KbSyncLog latest = kbSyncLogMapper.selectOne(latestWrapper);
        if (latest == null) {
            return new SyncStatusVo();
        }

        List<KbSyncLog> batchLogs = kbSyncLogMapper.selectList(new LambdaQueryWrapper<KbSyncLog>()
                .eq(KbSyncLog::getBatchNo, latest.getBatchNo())
                .eq(spaceId != null, KbSyncLog::getSpaceId, spaceId));

        SyncStatusVo vo = new SyncStatusVo();
        vo.setBatchNo(latest.getBatchNo());
        vo.setSpaceId(latest.getSpaceId());
        vo.setLastSyncTime(latest.getCreateTime());
        vo.setTotal(batchLogs.size());

        Map<String, Integer> counts = new LinkedHashMap<>();
        int fail = 0;
        for (KbSyncLog row : batchLogs) {
            if (row.getAction() != null) {
                counts.merge(row.getAction(), 1, Integer::sum);
            }
            if ("fail".equalsIgnoreCase(row.getStatus())) {
                fail++;
            }
        }
        vo.setActionCounts(counts);
        vo.setFailCount(fail);
        return vo;
    }

    @Override
    public SyncTriggerVo trigger(Long spaceId, String spaceCode) {
        if (!syncProperties.isEnabled()) {
            throw new BaseException("同步 API 已禁用（kb.sync.enabled=false）");
        }
        KbSpace space = resolveSpace(spaceId, spaceCode);
        assertSyncPermission(space.getId());
        return executeSync(space);
    }

    @Override
    public SyncTriggerVo triggerAfterEdit(Long spaceId) {
        if (!syncProperties.isEnabled()) {
            throw new BaseException("同步 API 已禁用（kb.sync.enabled=false）");
        }
        KbSpace space = resolveSpace(spaceId, null);
        kbAclService.assertCanEdit(space.getId());
        return executeSync(space);
    }

    @Override
    public SyncTriggerVo triggerScheduled() {
        if (!syncProperties.isEnabled()) {
            throw new BaseException("同步已禁用（kb.sync.enabled=false）");
        }
        KbSpace space = resolveSpace(null, syncProperties.getSpaceCode());
        return executeSync(space);
    }

    private SyncTriggerVo executeSync(KbSpace space) {
        Path script = resolveScriptPath();
        DbEndpoint db = parseJdbcUrl(datasourceUrl);

        List<String> command = new ArrayList<>();
        command.add(syncProperties.getPython());
        command.add(script.toString());
        command.add("--host");
        command.add(db.host);
        command.add("--port");
        command.add(String.valueOf(db.port));
        command.add("--user");
        command.add(dbUser);
        command.add("--password");
        command.add(dbPassword);
        command.add("--db");
        command.add(db.database);
        command.add("--space");
        command.add(space.getSpaceCode());
        command.add("--wiki-dir");
        command.add(resolveWikiDirForSpace(space.getSpaceCode()));

        SyncTriggerVo result = new SyncTriggerVo();
        result.setSpaceCode(space.getSpaceCode());

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                    log.info("[sync] {}", line);
                }
            }

            boolean finished = process.waitFor(syncProperties.getTimeoutSeconds(), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new BaseException("同步脚本超时（" + syncProperties.getTimeoutSeconds() + "s）");
            }

            int exitCode = process.exitValue();
            result.setExitCode(exitCode);
            result.setSuccess(exitCode == 0);
            result.setOutputTail(tail(output.toString(), 2000));
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("Sync trigger failed", e);
            throw new BaseException("触发同步失败：" + e.getMessage());
        }
        return result;
    }

    private void assertSyncPermission(Long spaceId) {
        if (kbAclService.isAdmin()) {
            return;
        }
        if (spaceId == null) {
            throw new BaseException("无权查看全库同步日志");
        }
        kbAclService.assertCanAdmin(spaceId);
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

    /** space_code → sync_to_db.py --wiki-dir（与 KbWikiProperties.spaceDirs 一致）。 */
    String resolveWikiDirForSpace(String spaceCode) {
        if (StringUtils.isBlank(spaceCode)) {
            throw new BaseException("空间编码不能为空");
        }
        String wikiDir = wikiProperties.getSpaceDirs().get(spaceCode);
        if (StringUtils.isBlank(wikiDir)) {
            throw new BaseException("空间未配置 wiki 目录映射: " + spaceCode);
        }
        return wikiDir;
    }

    private Path resolveScriptPath() {
        Path path = Paths.get(syncProperties.getScriptPath());
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        if (!path.toFile().exists()) {
            throw new BaseException("同步脚本不存在: " + path);
        }
        return path;
    }

    private String tail(String text, int maxLen) {
        if (text == null || text.length() <= maxLen) {
            return text;
        }
        return text.substring(text.length() - maxLen);
    }

    private DbEndpoint parseJdbcUrl(String url) {
        Matcher m = JDBC_MYSQL.matcher(url);
        if (!m.find()) {
            throw new BaseException("无法解析数据源 URL: " + url);
        }
        DbEndpoint ep = new DbEndpoint();
        ep.host = m.group(1);
        ep.port = m.group(2) == null ? 3306 : Integer.parseInt(m.group(2));
        ep.database = m.group(3);
        return ep;
    }

    private static class DbEndpoint {
        private String host;
        private int port;
        private String database;
    }
}
