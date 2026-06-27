package com.moli.knowledge.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.constant.CommonConstant;
import com.moli.common.core.IdGenerator;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbIngestProperties;
import com.moli.knowledge.server.config.KbWikiProperties;
import com.moli.knowledge.server.dto.IngestCommitResultVo;
import com.moli.knowledge.server.dto.IngestDraftUpdateRequest;
import com.moli.knowledge.server.dto.IngestDraftVo;
import com.moli.knowledge.server.dto.IngestGenerateResultVo;
import com.moli.knowledge.server.dto.IngestJobCreateRequest;
import com.moli.knowledge.server.dto.IngestJobFromTemplateRequest;
import com.moli.knowledge.server.dto.IngestJobVo;
import com.moli.knowledge.server.dto.IngestLintVo;
import com.moli.knowledge.server.dto.IngestPlanUpdateRequest;
import com.moli.knowledge.server.dto.IngestSaveAsTemplateRequest;
import com.moli.knowledge.server.dto.IngestTemplateCreateRequest;
import com.moli.knowledge.server.dto.IngestTemplateVo;
import com.moli.knowledge.server.dto.RawTreeNodeVo;
import com.moli.knowledge.server.dto.SyncTriggerVo;
import com.moli.knowledge.server.dto.WikiSaveRequest;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbIngestCommit;
import com.moli.knowledge.server.entity.KbIngestDraft;
import com.moli.knowledge.server.entity.KbIngestJob;
import com.moli.knowledge.server.entity.KbIngestPlan;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.enums.DocumentStatus;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.mapper.KbIngestCommitMapper;
import com.moli.knowledge.server.mapper.KbIngestDraftMapper;
import com.moli.knowledge.server.mapper.KbIngestJobMapper;
import com.moli.knowledge.server.mapper.KbIngestPlanMapper;
import com.moli.knowledge.server.entity.KbIngestTemplate;
import com.moli.knowledge.server.mapper.KbIngestTemplateMapper;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbIngestService;
import com.moli.knowledge.server.service.KbLlmClient;
import com.moli.knowledge.server.service.KbRawCoverageService;
import com.moli.knowledge.server.service.KbSyncService;
import com.moli.knowledge.server.service.KbWikiFileService;
import com.moli.knowledge.server.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class KbIngestServiceImpl implements KbIngestService {

    private static final String DEFAULT_SPACE_CODE = "enterprise-kb";

    /** 批次状态。 */
    private static final String ST_PLANNED = "planned";
    private static final String ST_REVIEWING = "reviewing";
    private static final String ST_COMMITTED = "committed";
    /** 草稿动作。 */
    private static final String ACT_CREATE = "create";
    private static final String ACT_ENRICH = "enrich";
    /** 草稿审批。 */
    private static final String AP_DRAFT = "draft";
    private static final String AP_APPROVED = "approved";
    private static final String AP_REJECTED = "rejected";

    private static final String PLAN_SYSTEM_PROMPT =
            "你是企业知识库的 Ingest 规划器（Planner）。范式：Karpathy LLM-Wiki，契约见 kb/AGENTS.md §4。\n"
            + "任务：读用户提供的 raw 源摘要 + 现有 wiki 候选页 slug，规划如何把 raw 融进已有 wiki。\n"
            + "硬性规则：\n"
            + "1) 只输出一个 JSON 对象，禁止任何解释、Markdown 代码围栏或正文。\n"
            + "2) 默认 enrich 优先（同主题已有页就补充，不新建重复页）；确无同主题页才 create。\n"
            + "3) 与已有页重复的 raw 放进 skip 并写原因。\n"
            + "4) 新旧结论矛盾放进 conflicts（只报告，不替用户决定）。\n"
            + "5) slug 用中文/英文/数字，词间连字符；create 的 sources 必须是给定的 raw 路径。\n"
            + "JSON 结构：{\"batchNo\":string,\"topic\":string,"
            + "\"create\":[{\"type\":\"article|guide|service|concept|interview\",\"slug\":string,\"title\":string,\"sources\":[string],\"reason\":string}],"
            + "\"enrich\":[{\"slug\":string,\"action\":\"append_section|rewrite_section\",\"reason\":string}],"
            + "\"skip\":[{\"raw\":string,\"reason\":string}],"
            + "\"edges\":[{\"from\":string,\"to\":string,\"type\":\"depends_on|relates_to|derived_from|supersedes|part_of\",\"evidence\":string}],"
            + "\"conflicts\":[string]}";

    private static final String PAGE_WRITER_PROMPT =
            "你是茉莉企业知识库的页面撰写器（PageWriter）。任务：为一个新 wiki 页输出**完整** markdown。\n"
            + "硬性规则（对齐 AGENTS.md §2）：\n"
            + "1) 只输出完整 markdown（YAML frontmatter + 正文），禁止任何解释、代码围栏、前后缀；\n"
            + "2) frontmatter 必填：title、slug、type、status(active)、tags、sources、related、created、updated；\n"
            + "3) slug 用给定 slug；type 用给定 type；sources 用给定 raw 路径；created/updated 用给定日期；\n"
            + "4) 正文 [[..]] 互链只用「同批次/已知 slug 列表」里的 slug，禁止乱造；\n"
            + "5) related：**仅**填 0–5 个与本页主题**强相关**的 slug（裸名）；优先与正文 [[..]] 互链一致；"
            + "无合适关联则 related: []；**禁止**把「已知 slug 列表」批量抄进 related；\n"
            + "6) 内容忠于给定 raw 源，提炼为结构化知识，不照抄全文。";

    private static final String ENRICH_WRITER_PROMPT =
            "你是茉莉企业知识库的增量补充器（EnrichWriter）。任务：给一篇已有 wiki 页补充一个新章节。\n"
            + "硬性规则：\n"
            + "1) 只输出**要追加的 markdown 章节**（从一个 `## 标题` 开始），禁止重复已有内容、禁止整页重写、禁止 frontmatter、禁止解释或代码围栏；\n"
            + "2) 内容忠于给定 raw 源，与已有正文不冲突；如发现冲突，在章节内用「> 注：」标注；\n"
            + "3) [[..]] 互链只用「同批次/已知 slug 列表」里的 slug。";

    /** kbType → wiki 子目录（复数）。 */
    private static final Map<String, String> TYPE_DIRS = buildTypeDirs();

    private static final Pattern WIKILINK = Pattern.compile("\\[\\[([^\\]]+)\\]\\]");
    private static final Pattern SOURCES_LINE = Pattern.compile("^sources:\\s*\\S", Pattern.MULTILINE);
    private static final Pattern RELATED_INLINE = Pattern.compile("^related:\\s*\\[(.*)]\\s*$", Pattern.MULTILINE);

    private static final int MAX_RELATED_SLUGS = 5;
    private static final int MAX_LINK_CANDIDATES = 25;

    private static Map<String, String> buildTypeDirs() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("guide", "guides");
        m.put("service", "services");
        m.put("concept", "concepts");
        m.put("article", "articles");
        m.put("interview", "interview");
        m.put("output", "outputs");
        m.put("exam", "exams");
        return m;
    }

    @Resource
    private KbIngestProperties ingestProperties;
    @Resource
    private KbWikiProperties wikiProperties;
    @Resource
    private KbIngestJobMapper jobMapper;
    @Resource
    private KbIngestPlanMapper planMapper;
    @Resource
    private KbIngestTemplateMapper templateMapper;
    @Resource
    private KbSpaceMapper kbSpaceMapper;
    @Resource
    private KbDocumentMapper kbDocumentMapper;
    @Resource
    private KbAclService kbAclService;
    @Resource
    private KbLlmClient llmClient;
    @Resource
    private KbIngestDraftMapper draftMapper;
    @Resource
    private KbIngestCommitMapper commitMapper;
    @Resource
    private KbWikiFileService kbWikiFileService;
    @Resource
    private KbSyncService kbSyncService;
    @Resource
    private KbRawCoverageService kbRawCoverageService;
    @Resource
    private PlatformTransactionManager transactionManager;

    /** 编程式事务：commit 的「文件落盘 + DB 记账」放一个事务，Sync 留到事务外。 */
    private TransactionTemplate txTemplate;

    @PostConstruct
    private void initTxTemplate() {
        this.txTemplate = new TransactionTemplate(transactionManager);
    }

    // ---------------------------------------------------------------- raw tree

    @Override
    public List<RawTreeNodeVo> rawTree(String prefix) {
        assertEnabled();
        // raw 归属 enterprise-kb，按该空间读权限放行
        kbAclService.assertCanRead(resolveSpace(null).getId());

        Path root = resolveRawRoot();
        Path base = normalizeUnder(root, prefix);
        if (!Files.isDirectory(base)) {
            throw new BaseException("目录不存在: " + (prefix == null ? "" : prefix));
        }
        int[] budget = {ingestProperties.getMaxTreeNodes()};
        return listChildren(root, base, budget);
    }

    private List<RawTreeNodeVo> listChildren(Path root, Path dir, int[] budget) {
        List<RawTreeNodeVo> nodes = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            List<Path> entries = stream
                    .filter(p -> !p.getFileName().toString().startsWith("."))
                    .sorted(Comparator
                            .comparing((Path p) -> Files.isDirectory(p) ? 0 : 1)
                            .thenComparing(p -> p.getFileName().toString()))
                    .collect(Collectors.toList());
            for (Path p : entries) {
                if (budget[0] <= 0) {
                    break;
                }
                budget[0]--;
                RawTreeNodeVo node = new RawTreeNodeVo();
                node.setName(p.getFileName().toString());
                node.setPath(root.relativize(p).toString().replace('\\', '/'));
                if (Files.isDirectory(p)) {
                    node.setType("dir");
                    node.setChildren(listChildren(root, p, budget));
                } else {
                    node.setType("file");
                    try {
                        node.setSize(Files.size(p));
                    } catch (IOException ignored) {
                        node.setSize(0L);
                    }
                }
                nodes.add(node);
            }
        } catch (IOException e) {
            throw new BaseException("读取 raw 目录失败：" + e.getMessage());
        }
        return nodes;
    }

    // ---------------------------------------------------------------- job CRUD

    @Override
    public IngestJobVo createJob(IngestJobCreateRequest request) {
        assertEnabled();
        if (request == null || StringUtils.isBlank(request.getTopic())) {
            throw new BaseException("主题不能为空");
        }
        if (request.getRawPaths() == null || request.getRawPaths().isEmpty()) {
            throw new BaseException("至少选择一个 raw 源");
        }
        KbSpace space = resolveSpace(request.getSpaceId());
        kbAclService.assertCanEdit(space.getId());

        Path root = resolveRawRoot();
        List<String> cleanPaths = new ArrayList<>();
        for (String rp : request.getRawPaths()) {
            Path target = normalizeUnder(root, rp);
            if (!Files.exists(target)) {
                throw new BaseException("raw 路径不存在: " + rp);
            }
            cleanPaths.add(root.relativize(target).toString().replace('\\', '/'));
        }

        KbIngestJob job = new KbIngestJob();
        job.setId(IdGenerator.getId());
        job.setSpaceId(space.getId());
        job.setBatchNo(StringUtils.isBlank(request.getBatchNo()) ? genBatchNo() : request.getBatchNo().trim());
        job.setTopic(request.getTopic().trim());
        job.setExpectTypes(StringUtils.trimToNull(request.getExpectTypes()));
        job.setRawPaths(JSON.toJSONString(cleanPaths));
        job.setStatus("created");
        job.setPlanVersion(0);
        job.setRemark(StringUtils.trimToNull(request.getRemark()));
        Long uid = ShiroUtils.getUserId();
        job.setCreateId(uid);
        job.setCreateTime(new Date());
        job.setUpdateId(uid);
        job.setUpdateTime(new Date());
        job.setIsDelete(CommonConstant.UN_DELETE);
        jobMapper.insert(job);

        log.info("[ingest] create job id={} space={} topic={} raws={}",
                job.getId(), space.getSpaceCode(), job.getTopic(), cleanPaths.size());
        return toVo(job, space, null);
    }

    @Override
    public Page<IngestJobVo> pageJobs(Long spaceId, String status, int pageNum, int pageSize) {
        assertEnabled();
        List<Long> accessible = kbAclService.accessibleSpaceIds();
        Page<IngestJobVo> result = new Page<>(pageNum, pageSize, 0);
        if (accessible.isEmpty()) {
            return result;
        }
        LambdaQueryWrapper<KbIngestJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KbIngestJob::getIsDelete, CommonConstant.UN_DELETE);
        wrapper.in(KbIngestJob::getSpaceId, accessible);
        if (spaceId != null) {
            wrapper.eq(KbIngestJob::getSpaceId, spaceId);
        }
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(KbIngestJob::getStatus, status);
        }
        wrapper.orderByDesc(KbIngestJob::getCreateTime);
        Page<KbIngestJob> page = jobMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        Map<Long, KbSpace> spaceMap = loadSpaceMap(page.getRecords().stream()
                .map(KbIngestJob::getSpaceId).collect(Collectors.toList()));
        List<IngestJobVo> vos = new ArrayList<>();
        for (KbIngestJob job : page.getRecords()) {
            vos.add(toVo(job, spaceMap.get(job.getSpaceId()), null));
        }
        result.setRecords(vos);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    @Override
    public IngestJobVo getJob(Long id) {
        assertEnabled();
        KbIngestJob job = loadJob(id);
        KbSpace space = resolveSpace(job.getSpaceId());
        kbAclService.assertCanRead(space.getId());
        return toVo(job, space, latestPlan(id));
    }

    // ---------------------------------------------------------------- plan

    @Override
    public IngestJobVo generatePlan(Long id) {
        assertEnabled();
        KbIngestJob job = loadJob(id);
        KbSpace space = resolveSpace(job.getSpaceId());
        kbAclService.assertCanEdit(space.getId());

        List<String> rawPaths = parsePaths(job.getRawPaths());
        String planJson;
        String source;
        String provider = null;
        String model = null;

        if (llmClient.usable()) {
            String userPrompt = buildPlanUserPrompt(job, space, rawPaths);
            String raw = llmClient.chat(PLAN_SYSTEM_PROMPT, userPrompt);
            JSONObject obj = parsePlanJson(raw);
            obj.put("batchNo", job.getBatchNo());
            obj.put("topic", job.getTopic());
            planJson = obj.toJSONString();
            source = "llm";
            provider = llmClient.getProvider();
            model = llmClient.getModel();
        } else {
            planJson = skeletonPlan(job, rawPaths);
            source = "skeleton";
            log.info("[ingest] LLM 未配置，生成可编辑骨架 plan job={}", id);
        }

        savePlanVersion(job, planJson, source, provider, model);
        return toVo(loadJob(id), space, latestPlan(id));
    }

    @Override
    public IngestJobVo updatePlan(Long id, IngestPlanUpdateRequest request) {
        assertEnabled();
        if (request == null || StringUtils.isBlank(request.getPlanJson())) {
            throw new BaseException("planJson 不能为空");
        }
        KbIngestJob job = loadJob(id);
        KbSpace space = resolveSpace(job.getSpaceId());
        kbAclService.assertCanEdit(space.getId());

        JSONObject obj;
        try {
            obj = JSON.parseObject(request.getPlanJson());
        } catch (Exception e) {
            throw new BaseException("planJson 不是合法的 JSON 对象");
        }
        if (obj == null) {
            throw new BaseException("planJson 不是合法的 JSON 对象");
        }
        savePlanVersion(job, obj.toJSONString(), "manual", null, null);
        return toVo(loadJob(id), space, latestPlan(id));
    }

    private void savePlanVersion(KbIngestJob job, String planJson, String source,
                                 String provider, String model) {
        int nextVersion = (job.getPlanVersion() == null ? 0 : job.getPlanVersion()) + 1;
        KbIngestPlan plan = new KbIngestPlan();
        plan.setId(IdGenerator.getId());
        plan.setJobId(job.getId());
        plan.setVersion(nextVersion);
        plan.setPlanJson(planJson);
        plan.setSource(source);
        plan.setProvider(provider);
        plan.setModel(model);
        plan.setCreateId(ShiroUtils.getUserId());
        plan.setCreateTime(new Date());
        planMapper.insert(plan);

        job.setPlanVersion(nextVersion);
        job.setStatus(ST_PLANNED);
        job.setUpdateId(ShiroUtils.getUserId());
        job.setUpdateTime(new Date());
        jobMapper.updateById(job);
    }

    // ---------------------------------------------------------------- export prompt

    @Override
    public String exportAgentPrompt(Long id) {
        assertEnabled();
        KbIngestJob job = loadJob(id);
        KbSpace space = resolveSpace(job.getSpaceId());
        kbAclService.assertCanRead(space.getId());
        List<String> rawPaths = parsePaths(job.getRawPaths());

        StringBuilder sb = new StringBuilder();
        sb.append("请按 kb/AGENTS.md §4 对以下 raw 源做 ingest 批次#").append(job.getBatchNo()).append("：\n");
        sb.append("- 空间：").append(space.getSpaceCode()).append("\n");
        sb.append("- 主题：").append(job.getTopic()).append("\n");
        if (StringUtils.isNotBlank(job.getExpectTypes())) {
            sb.append("- 期望类型：").append(job.getExpectTypes()).append("\n");
        }
        sb.append("- raw 源：\n");
        for (String rp : rawPaths) {
            sb.append("  - kb/raw/").append(rp).append("\n");
        }
        sb.append("- 与已有 wiki 合并，默认策略 A（enrich 优先）\n");
        sb.append("- 先输出规划（create/enrich/skip/conflicts），conflicts 等我确认再写盘\n");
        sb.append("- 只改 wiki/**，更新 index/log/edges；完成后 lint.py --strict 通过\n");

        KbIngestPlan plan = latestPlan(id);
        if (plan != null && StringUtils.isNotBlank(plan.getPlanJson())) {
            sb.append("\n参考已生成的 Plan（v").append(plan.getVersion()).append("）：\n```json\n");
            sb.append(plan.getPlanJson()).append("\n```\n");
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------- T15b 生成 / 审阅

    @Override
    public IngestGenerateResultVo generate(Long jobId, boolean resume) {
        assertEnabled();
        KbIngestJob job = loadJob(jobId);
        KbSpace space = resolveSpace(job.getSpaceId());
        kbAclService.assertCanEdit(space.getId());
        llmClient.assertUsable();

        KbIngestPlan plan = latestPlan(jobId);
        if (plan == null || StringUtils.isBlank(plan.getPlanJson())) {
            throw new BaseException("请先生成/确认 Plan，再生成草稿");
        }
        JSONObject planObj = JSON.parseObject(plan.getPlanJson());
        JSONArray create = planObj.getJSONArray("create");
        JSONArray enrich = planObj.getJSONArray("enrich");
        int total = (create == null ? 0 : create.size()) + (enrich == null ? 0 : enrich.size());
        if (total == 0) {
            throw new BaseException("Plan 的 create/enrich 均为空，无可生成页");
        }
        if (total > ingestProperties.getMaxPagesPerBatch()) {
            throw new BaseException("Plan 页数 " + total + " 超过单批上限 "
                    + ingestProperties.getMaxPagesPerBatch() + "，请拆分批次");
        }

        List<KbIngestDraft> existingDrafts = draftMapper.selectList(new LambdaQueryWrapper<KbIngestDraft>()
                .eq(KbIngestDraft::getJobId, jobId));
        Set<String> skipSlugs = new HashSet<>();
        if (resume) {
            for (KbIngestDraft existing : existingDrafts) {
                if (StringUtils.isNotBlank(resolveDraftContent(existing))) {
                    skipSlugs.add(existing.getSlug());
                }
            }
        }

        List<String> batchSlugs = collectPlanSlugs(create, enrich);
        List<String> linkCandidates = linkCandidateBareSlugs(space.getId(), job.getTopic(), batchSlugs);
        List<String> batchBareSlugs = batchBareNames(batchSlugs);
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        // 先在内存里生成（含 LLM 调用），单页失败隔离；成功后才在事务内替换，
        // 避免「先删旧草稿、生成中途失败」导致已有草稿丢失（P1）。
        List<KbIngestDraft> fresh = new ArrayList<>();
        int skipped = 0;
        int failed = 0;
        if (create != null) {
            for (int i = 0; i < create.size(); i++) {
                JSONObject item = create.getJSONObject(i);
                String relPath;
                try {
                    relPath = resolveCreateRelPath(item);
                } catch (Exception e) {
                    failed++;
                    log.warn("[ingest] generate create 项解析失败 job={}: {}", jobId, e.getMessage());
                    continue;
                }
                if (skipSlugs.contains(relPath)) {
                    skipped++;
                    continue;
                }
                try {
                    fresh.add(genCreateDraft(job, space, item, linkCandidates, batchBareSlugs, today));
                } catch (Exception e) {
                    failed++;
                    log.warn("[ingest] generate create 页失败 job={} slug={}: {}", jobId, relPath, e.getMessage());
                }
            }
        }
        if (enrich != null) {
            for (int i = 0; i < enrich.size(); i++) {
                JSONObject item = enrich.getJSONObject(i);
                String planSlug = StringUtils.trimToEmpty(item.getString("slug"));
                if (!planSlug.isEmpty()) {
                    String rel = resolveExistingRelPath(space.getSpaceCode(), planSlug);
                    String prospective = rel != null ? rel : typeDir("article") + "/" + planSlug;
                    if (skipSlugs.contains(prospective)) {
                        skipped++;
                        continue;
                    }
                }
                try {
                    fresh.add(genEnrichDraft(job, space, item, linkCandidates));
                } catch (Exception e) {
                    failed++;
                    log.warn("[ingest] generate enrich 页失败 job={} slug={}: {}", jobId, planSlug, e.getMessage());
                }
            }
        }

        // 全量模式若全部失败则保留原有草稿，不删除（避免数据丢失）
        if (!resume && fresh.isEmpty() && !existingDrafts.isEmpty()) {
            throw new BaseException("本次生成全部失败，已保留原有草稿；请检查 LLM 配置后重试");
        }

        // 事务内原子替换：(全量) 删旧 + 插新；(续跑) 覆盖同 slug 旧记录 + 插新
        final boolean fResume = resume;
        txTemplate.execute(status -> {
            if (!fResume) {
                draftMapper.delete(new LambdaQueryWrapper<KbIngestDraft>().eq(KbIngestDraft::getJobId, jobId));
            }
            for (KbIngestDraft d : fresh) {
                if (fResume) {
                    draftMapper.delete(new LambdaQueryWrapper<KbIngestDraft>()
                            .eq(KbIngestDraft::getJobId, jobId)
                            .eq(KbIngestDraft::getSlug, d.getSlug()));
                }
                draftMapper.insert(d);
            }
            job.setStatus(ST_REVIEWING);
            job.setUpdateId(ShiroUtils.getUserId());
            job.setUpdateTime(new Date());
            jobMapper.updateById(job);
            return null;
        });

        IngestGenerateResultVo result = new IngestGenerateResultVo();
        result.setTotal(total);
        result.setGenerated(fresh.size());
        result.setSkipped(skipped);
        result.setFailed(failed);
        result.setResume(resume);
        result.setDrafts(listDrafts(jobId));
        log.info("[ingest] generate job={} resume={} generated={} skipped={} failed={}",
                jobId, resume, fresh.size(), skipped, failed);
        return result;
    }

    private String resolveCreateRelPath(JSONObject item) {
        String type = StringUtils.defaultIfBlank(item.getString("type"), "article");
        String bare = StringUtils.trimToEmpty(item.getString("slug"));
        if (bare.isEmpty()) {
            throw new BaseException("create 项缺少 slug，请先在 Plan 补全");
        }
        return typeDir(type) + "/" + bare;
    }

    private KbIngestDraft genCreateDraft(KbIngestJob job, KbSpace space, JSONObject item,
                                         List<String> linkCandidates, List<String> batchBareSlugs,
                                         String today) {
        String type = StringUtils.defaultIfBlank(item.getString("type"), "article");
        String bare = StringUtils.trimToEmpty(item.getString("slug"));
        if (bare.isEmpty()) {
            throw new BaseException("create 项缺少 slug，请先在 Plan 补全");
        }
        String relPath = resolveCreateRelPath(item);
        List<String> sources = jsonStrList(item.getJSONArray("sources"));
        List<String> planRelated = jsonStrList(item.getJSONArray("related"));

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("目标 slug：").append(bare).append('\n');
        userPrompt.append("type：").append(type).append('\n');
        userPrompt.append("title：").append(StringUtils.defaultString(item.getString("title"))).append('\n');
        userPrompt.append("created/updated：").append(today).append('\n');
        userPrompt.append("sources（写入 frontmatter）：\n").append(bulletList(sources)).append('\n');
        if (!planRelated.isEmpty()) {
            userPrompt.append("planRelated（优先采用，0–5 个）：\n").append(bulletList(planRelated)).append('\n');
        }
        userPrompt.append("已知 slug 列表（正文 [[..]] 互链可用，勿整批写入 related）：\n")
                .append(bulletList(limit(linkCandidates, MAX_LINK_CANDIDATES))).append('\n');
        userPrompt.append("\nraw 源内容（已截断）：\n").append(readSources(sources));

        String content = stripFence(llmClient.chat(PAGE_WRITER_PROMPT, userPrompt.toString()));
        content = sanitizeRelatedFrontmatter(content, bareSlug(bare), batchBareSlugs, planRelated);

        KbIngestDraft d = newDraft(job, relPath, type, ACT_CREATE);
        d.setBaseline("");
        d.setPatch(null);
        d.setDraft(content);
        return d;
    }

    private KbIngestDraft genEnrichDraft(KbIngestJob job, KbSpace space, JSONObject item,
                                         List<String> knownSlugs) {
        String planSlug = StringUtils.trimToEmpty(item.getString("slug"));
        if (planSlug.isEmpty()) {
            throw new BaseException("enrich 项缺少 slug");
        }
        String relPath = resolveExistingRelPath(space.getSpaceCode(), planSlug);
        String baseline = "";
        String kbType = inferTypeFromRelPath(relPath);
        if (relPath != null) {
            baseline = readWikiFile(space.getSpaceCode(), relPath);
        } else {
            // 找不到已有页：降级为 create（落在 articles 下，需人工确认）
            relPath = typeDir("article") + "/" + planSlug;
            kbType = "article";
        }

        String reason = StringUtils.defaultString(item.getString("reason"));
        String userPrompt = "目标页 slug：" + planSlug + "\n"
                + "补充原因：" + reason + "\n"
                + "已知 slug 列表（互链可用，勿写入 related）：\n" + bulletList(limit(knownSlugs, MAX_LINK_CANDIDATES))
                + "\n\n已有页当前全文：\n" + (baseline.isEmpty() ? "（页不存在，请作为新页主体内容输出一个章节）" : baseline);

        String section = stripFence(llmClient.chat(ENRICH_WRITER_PROMPT, userPrompt));
        boolean isEnrich = StringUtils.isNotBlank(baseline);
        String action = isEnrich ? ACT_ENRICH : ACT_CREATE;

        KbIngestDraft d = newDraft(job, relPath, kbType, action);
        d.setBaseline(baseline);
        if (isEnrich) {
            d.setPatch(section);
            d.setDraft(mergeEnrich(baseline, section));
        } else {
            d.setPatch(null);
            d.setDraft(section);
        }
        return d;
    }

    @Override
    public List<IngestDraftVo> listDrafts(Long jobId) {
        assertEnabled();
        KbIngestJob job = loadJob(jobId);
        kbAclService.assertCanRead(job.getSpaceId());
        return draftMapper.selectList(new LambdaQueryWrapper<KbIngestDraft>()
                        .eq(KbIngestDraft::getJobId, jobId)
                        .orderByAsc(KbIngestDraft::getId))
                .stream().map(this::toDraftVo).collect(Collectors.toList());
    }

    @Override
    public IngestDraftVo getDraft(Long jobId, String slug) {
        assertEnabled();
        KbIngestJob job = loadJob(jobId);
        kbAclService.assertCanRead(job.getSpaceId());
        return toDraftVo(loadDraft(jobId, slug));
    }

    @Override
    public IngestDraftVo updateDraft(Long jobId, String slug, IngestDraftUpdateRequest request) {
        assertEnabled();
        if (request == null) {
            throw new BaseException("请求不能为空");
        }
        KbIngestJob job = loadJob(jobId);
        kbAclService.assertCanEdit(job.getSpaceId());
        KbIngestDraft d = loadDraft(jobId, slug);

        if (ACT_ENRICH.equals(d.getAction()) && StringUtils.isNotBlank(request.getPatch())) {
            d.setPatch(request.getPatch());
            d.setDraft(mergeEnrich(StringUtils.defaultString(d.getBaseline()), request.getPatch()));
        } else if (StringUtils.isNotBlank(request.getContent())) {
            d.setDraft(request.getContent());
            if (ACT_ENRICH.equals(d.getAction())) {
                d.setPatch(null);
            }
        } else {
            throw new BaseException("content 或 patch 不能为空");
        }
        d.setApproval(AP_DRAFT);
        d.setUpdateTime(new Date());
        draftMapper.updateById(d);
        return toDraftVo(d);
    }

    @Override
    public IngestDraftVo regenerateDraft(Long jobId, String slug) {
        assertEnabled();
        KbIngestJob job = loadJob(jobId);
        KbSpace space = resolveSpace(job.getSpaceId());
        kbAclService.assertCanEdit(space.getId());
        llmClient.assertUsable();
        KbIngestDraft old = loadDraft(jobId, slug);

        KbIngestPlan plan = latestPlan(jobId);
        JSONObject planObj = plan != null ? JSON.parseObject(plan.getPlanJson()) : new JSONObject();
        List<String> batchSlugs = collectPlanSlugs(planObj.getJSONArray("create"), planObj.getJSONArray("enrich"));
        List<String> linkCandidates = linkCandidateBareSlugs(space.getId(), job.getTopic(), batchSlugs);
        List<String> batchBareSlugs = batchBareNames(batchSlugs);
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        JSONObject item = findPlanItem(jobId, slug, old.getAction());

        KbIngestDraft fresh;
        if (ACT_ENRICH.equals(old.getAction())) {
            fresh = genEnrichDraft(job, space, item, linkCandidates);
        } else {
            fresh = genCreateDraft(job, space, item, linkCandidates, batchBareSlugs, today);
        }
        old.setBaseline(fresh.getBaseline());
        old.setPatch(fresh.getPatch());
        old.setDraft(fresh.getDraft());
        old.setApproval(AP_DRAFT);
        old.setUpdateTime(new Date());
        draftMapper.updateById(old);
        return toDraftVo(old);
    }

    @Override
    public IngestDraftVo setApproval(Long jobId, String slug, String approval) {
        assertEnabled();
        if (!AP_APPROVED.equals(approval) && !AP_REJECTED.equals(approval) && !AP_DRAFT.equals(approval)) {
            throw new BaseException("非法审批状态: " + approval);
        }
        KbIngestJob job = loadJob(jobId);
        kbAclService.assertCanEdit(job.getSpaceId());
        KbIngestDraft d = loadDraft(jobId, slug);
        d.setApproval(approval);
        d.setUpdateTime(new Date());
        draftMapper.updateById(d);
        return toDraftVo(d);
    }

    // ---------------------------------------------------------------- T15c lint + commit

    @Override
    public IngestLintVo lint(Long jobId) {
        assertEnabled();
        KbIngestJob job = loadJob(jobId);
        KbSpace space = resolveSpace(job.getSpaceId());
        kbAclService.assertCanRead(space.getId());

        List<KbIngestDraft> drafts = draftMapper.selectList(new LambdaQueryWrapper<KbIngestDraft>()
                .eq(KbIngestDraft::getJobId, jobId));
        return lintDrafts(space, drafts, enrichPlanSlugs(jobId));
    }

    /** 取最新 plan 中 enrich[] 的 bare slug（小写），用于检测「enrich 降级为 create」。 */
    private Set<String> enrichPlanSlugs(Long jobId) {
        Set<String> set = new HashSet<>();
        KbIngestPlan plan = latestPlan(jobId);
        if (plan == null || StringUtils.isBlank(plan.getPlanJson())) {
            return set;
        }
        JSONArray enrich = JSON.parseObject(plan.getPlanJson()).getJSONArray("enrich");
        if (enrich == null) {
            return set;
        }
        for (int i = 0; i < enrich.size(); i++) {
            String s = StringUtils.trimToEmpty(enrich.getJSONObject(i).getString("slug"));
            if (!s.isEmpty()) {
                set.add(bareSlug(s).toLowerCase(Locale.ROOT));
            }
        }
        return set;
    }

    /** lint 核心：复用预读的 drafts，避免 commit 时二次全表扫描（P2）。 */
    private IngestLintVo lintDrafts(KbSpace space, List<KbIngestDraft> drafts, Set<String> enrichPlanSlugs) {
        IngestLintVo vo = new IngestLintVo();
        List<IngestLintVo.Item> items = new ArrayList<>();
        if (drafts.isEmpty()) {
            items.add(new IngestLintVo.Item(null, "no_draft", "ERROR", "尚无草稿，请先生成"));
        }

        // 已知 slug = DB 既有 + 本批次新建/enrich 的 bare slug
        Set<String> known = new HashSet<>();
        Set<String> titleIndex = new HashSet<>();
        indexDocuments(space.getId(), known, titleIndex);
        for (KbIngestDraft d : drafts) {
            known.add(bareSlug(d.getSlug()).toLowerCase(Locale.ROOT));
        }

        boolean allApproved = !drafts.isEmpty();
        for (KbIngestDraft d : drafts) {
            if (!AP_APPROVED.equals(d.getApproval())) {
                allApproved = false;
            }
            if (AP_REJECTED.equals(d.getApproval())) {
                continue;
            }
            items.addAll(lintDraft(d, known, titleIndex, enrichPlanSlugs));
        }

        int blocking = (int) items.stream().filter(i -> "ERROR".equals(i.getSeverity())).count();
        vo.setIssues(items);
        vo.setIssueCount(items.size());
        vo.setBlockingCount(blocking);
        vo.setCommitReady(blocking == 0 && allApproved);
        return vo;
    }

    private List<IngestLintVo.Item> lintDraft(KbIngestDraft d, Set<String> known, Set<String> titleIndex,
                                              Set<String> enrichPlanSlugs) {
        List<IngestLintVo.Item> items = new ArrayList<>();
        String content = resolveDraftContent(d);
        String slug = d.getSlug();
        boolean enrich = ACT_ENRICH.equals(d.getAction());

        // 计划为 enrich 但落成 create：说明未找到目标已有页，已降级为新建，提示人工确认落点
        if (ACT_CREATE.equals(d.getAction())
                && enrichPlanSlugs.contains(bareSlug(slug).toLowerCase(Locale.ROOT))) {
            items.add(new IngestLintVo.Item(slug, "enrich_downgraded", "WARN",
                    "计划为 enrich，但未找到可补充的已有页，已降级为新建（落在 articles/），请确认落点目录"));
        }

        // frontmatter 仅对整页（create / enrich 合并后整页）校验
        if (!content.startsWith("---")) {
            items.add(new IngestLintVo.Item(slug, "missing_frontmatter", "ERROR",
                    "缺少 YAML frontmatter（应以 --- 开头）"));
        } else {
            int end = content.indexOf("\n---", 3);
            if (end < 0) {
                items.add(new IngestLintVo.Item(slug, "missing_frontmatter", "ERROR",
                        "frontmatter 未闭合（缺少第二个 ---）"));
            } else {
                String fm = content.substring(0, end + 4);
                if (!fm.contains("title:")) {
                    items.add(new IngestLintVo.Item(slug, "missing_frontmatter", "ERROR", "缺少 title"));
                }
                if (!fm.contains("slug:")) {
                    items.add(new IngestLintVo.Item(slug, "missing_frontmatter", "ERROR", "缺少 slug"));
                }
                if (!fm.contains("type:")) {
                    items.add(new IngestLintVo.Item(slug, "missing_frontmatter", "WARN", "缺少 type"));
                }
                if (!SOURCES_LINE.matcher(fm).find()) {
                    items.add(new IngestLintVo.Item(slug, "empty_sources",
                            enrich ? "WARN" : "ERROR", "frontmatter sources 为空或缺失"));
                }
                int relatedCount = countRelatedInFrontmatter(content);
                if (relatedCount > MAX_RELATED_SLUGS) {
                    items.add(new IngestLintVo.Item(slug, "related_overflow", "WARN",
                            "related 条目过多（" + relatedCount + "），建议 ≤" + MAX_RELATED_SLUGS
                                    + " 且仅填正文互链/同批次相关页"));
                }
            }
        }

        Matcher m = WIKILINK.matcher(content);
        Set<String> seen = new HashSet<>();
        while (m.find()) {
            String target = m.group(1).split("\\|")[0].trim();
            if (target.isEmpty() || !seen.add(target)) {
                continue;
            }
            if (!resolvesLink(target, known, titleIndex)) {
                items.add(new IngestLintVo.Item(slug, "broken_link", "WARN",
                        "断链：`[[" + target + "]]` 未找到对应页（slug/标题/批次内）"));
            }
        }
        return items;
    }

    @Override
    public IngestCommitResultVo commit(Long jobId, boolean sync) {
        assertEnabled();
        KbIngestJob job = loadJob(jobId);
        KbSpace space = resolveSpace(job.getSpaceId());
        kbAclService.assertCanEdit(space.getId());

        // 1) 门禁（只读）：lint + 批准状态，drafts 只读一次复用（P2）
        List<KbIngestDraft> drafts = draftMapper.selectList(new LambdaQueryWrapper<KbIngestDraft>()
                .eq(KbIngestDraft::getJobId, jobId));
        IngestLintVo lint = lintDrafts(space, drafts, enrichPlanSlugs(jobId));
        if (lint.getBlockingCount() > 0) {
            throw new BaseException("lint 存在 " + lint.getBlockingCount() + " 个 ERROR，禁止提交");
        }
        List<KbIngestDraft> approved = drafts.stream()
                .filter(d -> AP_APPROVED.equals(d.getApproval()))
                .collect(Collectors.toList());
        if (approved.isEmpty()) {
            throw new BaseException("没有已批准的页，禁止提交");
        }
        if (drafts.stream().anyMatch(d -> AP_DRAFT.equals(d.getApproval()))) {
            throw new BaseException("存在未审阅（draft）的页，请先批准或拒绝");
        }

        // 2) 文件落盘 + DB 记账：编程式事务（治理文件追加幂等，重复 commit 不重复写）
        //    DB 失败回滚，文件因幂等可被再次 commit 安全覆盖/跳过。
        CommitHolder holder = txTemplate.execute(status -> doCommit(job, space, approved));

        IngestCommitResultVo vo = holder.vo;

        // 3) Sync 留到事务提交之后执行，避免外部子进程长时间占用 DB 事务（P1）
        if (sync) {
            try {
                SyncTriggerVo sr = kbSyncService.trigger(space.getId(), space.getSpaceCode());
                vo.setSyncTriggered(true);
                vo.setSyncResult(sr);
                holder.commit.setSyncBatchNo(sr != null ? space.getSpaceCode() : null);
                commitMapper.updateById(holder.commit);
            } catch (Exception e) {
                log.warn("[ingest] commit 后 Sync 失败 job={}: {}", jobId, e.getMessage());
                SyncTriggerVo sr = new SyncTriggerVo();
                sr.setSuccess(false);
                sr.setSpaceCode(space.getSpaceCode());
                sr.setOutputTail("Sync 触发失败：" + e.getMessage());
                vo.setSyncTriggered(true);
                vo.setSyncResult(sr);
            }
        }

        log.info("[ingest] commit job={} created={} updated={} edges={} sync={}",
                jobId, vo.getCreated(), vo.getUpdated(), vo.getEdgesAppended(), sync);
        kbRawCoverageService.invalidateCache(space.getId());
        return vo;
    }

    /** commit 事务体返回值（vo + commit 实体，便于事务外补 syncBatchNo）。 */
    private static final class CommitHolder {
        private final IngestCommitResultVo vo;
        private final KbIngestCommit commit;
        private CommitHolder(IngestCommitResultVo vo, KbIngestCommit commit) {
            this.vo = vo;
            this.commit = commit;
        }
    }

    /** 文件落盘 + DB 记账（同一事务）。治理文件追加均幂等。 */
    private CommitHolder doCommit(KbIngestJob job, KbSpace space, List<KbIngestDraft> approved) {
        IngestCommitResultVo vo = new IngestCommitResultVo();
        vo.setJobId(job.getId());
        List<String> files = new ArrayList<>();
        int created = 0;
        int updated = 0;

        // 1. 写 wiki 各页（writePage 覆盖写，天然幂等）
        for (KbIngestDraft d : approved) {
            WikiSaveRequest req = new WikiSaveRequest();
            req.setSlug(d.getSlug());
            req.setSpaceId(space.getId());
            req.setContent(resolveDraftContent(d));
            req.setChangeLog("ingest 批次#" + job.getBatchNo());
            kbWikiFileService.writePage(req);
            files.add(resolveWikiDir(space.getSpaceCode()) + "/" + d.getSlug() + ".md");
            if (ACT_ENRICH.equals(d.getAction())) {
                updated++;
            } else {
                created++;
            }
        }
        vo.setCreated(created);
        vo.setUpdated(updated);
        vo.setFiles(files);

        // 2~4. 治理文件追加（幂等：按 job 标记 / 整行去重）
        vo.setEdgesAppended(appendEdges(space.getSpaceCode(), job, approved));
        vo.setLogAppended(appendLog(space.getSpaceCode(), job, approved));
        vo.setIndexUpdated(appendIndexSection(space.getSpaceCode(), job, approved));

        // 5. 记录 commit + 置状态
        KbIngestCommit commit = new KbIngestCommit();
        commit.setId(IdGenerator.getId());
        commit.setJobId(job.getId());
        commit.setFilesJson(JSON.toJSONString(files));
        commit.setCreateId(ShiroUtils.getUserId());
        commit.setCreateTime(new Date());
        commitMapper.insert(commit);

        job.setStatus(ST_COMMITTED);
        job.setUpdateId(ShiroUtils.getUserId());
        job.setUpdateTime(new Date());
        jobMapper.updateById(job);

        return new CommitHolder(vo, commit);
    }

    // ---------------------------------------------------------------- 落盘文件操作

    private int appendEdges(String spaceCode, KbIngestJob job, List<KbIngestDraft> approved) {
        KbIngestPlan plan = latestPlan(job.getId());
        if (plan == null) {
            return 0;
        }
        JSONArray edges = JSON.parseObject(plan.getPlanJson()).getJSONArray("edges");
        if (edges == null || edges.isEmpty()) {
            return 0;
        }
        Set<String> approvedBare = approved.stream()
                .map(d -> bareSlug(d.getSlug())).collect(Collectors.toSet());
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        // 幂等：跳过已存在于 edges.jsonl 的相同行（重复 commit 不重复写）
        String existing = readWikiRelFile(spaceCode, "graph/edges.jsonl");
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = 0; i < edges.size(); i++) {
            JSONObject e = edges.getJSONObject(i);
            String from = StringUtils.trimToEmpty(e.getString("from"));
            String to = StringUtils.trimToEmpty(e.getString("to"));
            // 至少一端是本批次新页才追加，避免污染
            if (!approvedBare.contains(bareSlug(from)) && !approvedBare.contains(bareSlug(to))) {
                continue;
            }
            JSONObject line = new JSONObject(true);
            line.put("from", from);
            line.put("to", to);
            line.put("type", StringUtils.defaultIfBlank(e.getString("type"), "relates_to"));
            line.put("evidence", StringUtils.defaultString(e.getString("evidence")));
            line.put("date", today);
            String json = line.toJSONString();
            if (existing.contains(json) || sb.indexOf(json) >= 0) {
                continue;
            }
            sb.append(json).append('\n');
            count++;
        }
        if (count == 0) {
            return 0;
        }
        appendToFile(spaceCode, "graph/edges.jsonl", sb.toString());
        return count;
    }

    private boolean appendLog(String spaceCode, KbIngestJob job, List<KbIngestDraft> approved) {
        // 幂等：同一 job 的批次行只写一次
        String marker = ingestMarker(job);
        if (readWikiRelFile(spaceCode, "log.md").contains(marker)) {
            return false;
        }
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String creates = approved.stream().filter(d -> ACT_CREATE.equals(d.getAction()))
                .map(d -> bareSlug(d.getSlug())).collect(Collectors.joining(", "));
        String enriches = approved.stream().filter(d -> ACT_ENRICH.equals(d.getAction()))
                .map(d -> bareSlug(d.getSlug())).collect(Collectors.joining(", "));
        StringBuilder line = new StringBuilder();
        line.append("## [").append(today).append("] ingest | 批次#").append(job.getBatchNo())
                .append(' ').append(job.getTopic()).append(" (Web工作台)");
        if (!creates.isEmpty()) {
            line.append(" → create ").append(creates);
        }
        if (!enriches.isEmpty()) {
            line.append("; enrich ").append(enriches);
        }
        line.append(' ').append(marker).append('\n');
        appendToFile(spaceCode, "log.md", line.toString());
        return true;
    }

    private boolean appendIndexSection(String spaceCode, KbIngestJob job, List<KbIngestDraft> approved) {
        // 幂等：同一 job 的批次段只写一次
        String marker = ingestMarker(job);
        if (readWikiRelFile(spaceCode, "index.md").contains(marker)) {
            return false;
        }
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        StringBuilder sb = new StringBuilder();
        sb.append("\n## 批次 #").append(job.getBatchNo()).append("（Web Ingest ").append(today).append("）")
                .append(' ').append(marker).append("\n\n");
        for (KbIngestDraft d : approved) {
            sb.append("- [[").append(bareSlug(d.getSlug())).append("]] — ")
                    .append(ACT_ENRICH.equals(d.getAction()) ? "enrich" : ("create " + StringUtils.defaultString(d.getKbType())))
                    .append('\n');
        }
        appendToFile(spaceCode, "index.md", sb.toString());
        return true;
    }

    /** 治理文件幂等标记（HTML 注释，渲染不可见）。 */
    private String ingestMarker(KbIngestJob job) {
        return "<!-- ingest-job:" + job.getId() + " -->";
    }

    /** 读 wiki 目录下相对文件全文，不存在/失败返回 ""。 */
    private String readWikiRelFile(String spaceCode, String relFile) {
        Path file = resolveWikiRelFile(spaceCode, relFile);
        if (!Files.exists(file)) {
            return "";
        }
        try {
            return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    /** 向 wiki 目录下相对文件追加内容（不存在则创建）。 */
    private void appendToFile(String spaceCode, String relFile, String text) {
        Path file = resolveWikiRelFile(spaceCode, relFile);
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Files.write(file, text.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new BaseException("写入 " + relFile + " 失败：" + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- helpers

    private String buildPlanUserPrompt(KbIngestJob job, KbSpace space, List<String> rawPaths) {
        StringBuilder sb = new StringBuilder();
        sb.append("批次#").append(job.getBatchNo()).append("\n");
        sb.append("主题：").append(job.getTopic()).append("\n");
        if (StringUtils.isNotBlank(job.getExpectTypes())) {
            sb.append("期望类型：").append(job.getExpectTypes()).append("\n");
        }

        List<String> candidates = candidateSlugs(space.getId(), job.getTopic());
        sb.append("\n现有 wiki 候选页（用于去重 / enrich，可能为空）：\n");
        if (candidates.isEmpty()) {
            sb.append("（无匹配候选）\n");
        } else {
            for (String c : candidates) {
                sb.append("- ").append(c).append("\n");
            }
        }

        Path root = resolveRawRoot();
        sb.append("\nraw 源内容（已截断）：\n");
        for (String rp : rawPaths) {
            sb.append("\n===== raw/").append(rp).append(" =====\n");
            sb.append(readSnippet(normalizeUnder(root, rp)));
            sb.append("\n");
        }
        sb.append("\n请输出 Plan JSON（最多 ").append(ingestProperties.getMaxPagesPerBatch())
                .append(" 页 create+enrich）。");
        return sb.toString();
    }

    /** 用主题关键词在该空间已发布文档里召回候选页 slug（title#slug），供去重。 */
    private List<String> candidateSlugs(Long spaceId, String topic) {
        try {
            List<KbDocument> docs = kbDocumentMapper.searchAskCandidates(
                    java.util.Collections.singletonList(spaceId),
                    DocumentStatus.PUBLISHED.getCode(),
                    null, null, topic, 20);
            List<String> out = new ArrayList<>();
            if (docs != null) {
                for (KbDocument d : docs) {
                    out.add(StringUtils.defaultString(d.getKbType()) + "/" + d.getSlug()
                            + "（" + StringUtils.defaultString(d.getTitle()) + "）");
                }
            }
            return out;
        } catch (Exception e) {
            log.warn("[ingest] 候选页召回失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String readSnippet(Path file) {
        if (!Files.isRegularFile(file)) {
            return "（非文本文件或不存在，跳过）";
        }
        try {
            String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            int max = ingestProperties.getRawSnippetChars();
            if (content.length() > max) {
                return content.substring(0, max) + "\n……（已截断）";
            }
            return content;
        } catch (IOException e) {
            return "（读取失败：" + e.getMessage() + "）";
        }
    }

    /** LLM 输出去围栏并解析为 JSON 对象。 */
    private JSONObject parsePlanJson(String raw) {
        if (StringUtils.isBlank(raw)) {
            throw new BaseException("Plan 生成为空");
        }
        String s = raw.trim();
        if (s.startsWith("```")) {
            int nl = s.indexOf('\n');
            if (nl > 0) {
                s = s.substring(nl + 1);
            }
            if (s.endsWith("```")) {
                s = s.substring(0, s.length() - 3);
            }
            s = s.trim();
        }
        // 容错：截取第一个 { 到最后一个 }
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start >= 0 && end > start) {
            s = s.substring(start, end + 1);
        }
        try {
            JSONObject obj = JSON.parseObject(s);
            if (obj == null) {
                throw new BaseException("Plan 不是 JSON 对象");
            }
            return obj;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException("Plan JSON 解析失败：" + e.getMessage());
        }
    }

    private String skeletonPlan(KbIngestJob job, List<String> rawPaths) {
        JSONObject obj = new JSONObject(true);
        obj.put("batchNo", job.getBatchNo());
        obj.put("topic", job.getTopic());
        JSONArray create = new JSONArray();
        for (String rp : rawPaths) {
            JSONObject item = new JSONObject(true);
            item.put("type", StringUtils.isNotBlank(job.getExpectTypes())
                    ? job.getExpectTypes().split(",")[0].trim() : "article");
            item.put("slug", "");
            item.put("title", "");
            JSONArray sources = new JSONArray();
            sources.add("raw/" + rp);
            item.put("sources", sources);
            item.put("reason", "LLM 未配置，请人工规划");
            create.add(item);
        }
        obj.put("create", create);
        obj.put("enrich", new JSONArray());
        obj.put("skip", new JSONArray());
        obj.put("edges", new JSONArray());
        obj.put("conflicts", new JSONArray());
        return obj.toJSONString();
    }

    private List<String> parsePaths(String json) {
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            return JSON.parseArray(json, String.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private KbIngestJob loadJob(Long id) {
        if (id == null) {
            throw new BaseException("批次ID不能为空");
        }
        KbIngestJob job = jobMapper.selectById(id);
        if (job == null || (job.getIsDelete() != null && job.getIsDelete() == 1)) {
            throw new BaseException("批次不存在");
        }
        return job;
    }

    private KbIngestPlan latestPlan(Long jobId) {
        return planMapper.selectOne(new LambdaQueryWrapper<KbIngestPlan>()
                .eq(KbIngestPlan::getJobId, jobId)
                .orderByDesc(KbIngestPlan::getVersion)
                .last("limit 1"));
    }

    private IngestJobVo toVo(KbIngestJob job, KbSpace space, KbIngestPlan plan) {
        IngestJobVo vo = new IngestJobVo();
        vo.setId(job.getId());
        vo.setSpaceId(job.getSpaceId());
        vo.setSpaceCode(space == null ? null : space.getSpaceCode());
        vo.setBatchNo(job.getBatchNo());
        vo.setTopic(job.getTopic());
        vo.setExpectTypes(job.getExpectTypes());
        vo.setRawPaths(parsePaths(job.getRawPaths()));
        vo.setStatus(job.getStatus());
        vo.setPlanVersion(job.getPlanVersion());
        vo.setRemark(job.getRemark());
        vo.setCreateTime(job.getCreateTime());
        vo.setUpdateTime(job.getUpdateTime());
        if (plan != null) {
            vo.setPlanJson(plan.getPlanJson());
            vo.setPlanSource(plan.getSource());
        }
        try {
            vo.setCanEdit(kbAclService.canEdit(job.getSpaceId()));
        } catch (Exception e) {
            vo.setCanEdit(false);
        }
        return vo;
    }

    private String genBatchNo() {
        return "WB-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    private void assertEnabled() {
        if (!ingestProperties.isEnabled()) {
            throw new BaseException("Ingest 工作台已禁用（kb.ingest.enabled=false）");
        }
    }

    private KbSpace resolveSpace(Long spaceId) {
        if (spaceId != null) {
            KbSpace space = kbSpaceMapper.selectById(spaceId);
            if (space == null) {
                throw new BaseException("空间不存在");
            }
            return space;
        }
        KbSpace space = kbSpaceMapper.selectOne(new LambdaQueryWrapper<KbSpace>()
                .eq(KbSpace::getSpaceCode, DEFAULT_SPACE_CODE)
                .last("limit 1"));
        if (space == null) {
            throw new BaseException("默认空间不存在: " + DEFAULT_SPACE_CODE);
        }
        return space;
    }

    private Path resolveRawRoot() {
        Path root = Paths.get(ingestProperties.getRawRoot());
        if (!root.isAbsolute()) {
            root = Paths.get(System.getProperty("user.dir")).resolve(root);
        }
        return root.normalize();
    }

    /** 解析 prefix/相对路径并校验在 raw 根内（防目录穿越）。 */
    private Path normalizeUnder(Path root, String relative) {
        Path target;
        if (StringUtils.isBlank(relative)) {
            target = root;
        } else {
            String rel = relative.trim().replace('\\', '/');
            if (rel.startsWith("/") || rel.contains("..") || rel.contains(":")) {
                throw new BaseException("非法路径（越权）: " + relative);
            }
            target = root.resolve(rel).normalize();
        }
        if (!target.startsWith(root)) {
            throw new BaseException("非法路径（越权）: " + relative);
        }
        return target;
    }

    // ---------------------------------------------------------------- T15b/c helpers

    private KbIngestDraft newDraft(KbIngestJob job, String relPath, String kbType, String action) {
        KbIngestDraft d = new KbIngestDraft();
        d.setId(IdGenerator.getId());
        d.setJobId(job.getId());
        d.setSlug(relPath);
        d.setKbType(kbType);
        d.setAction(action);
        d.setApproval(AP_DRAFT);
        d.setCreateId(ShiroUtils.getUserId());
        d.setCreateTime(new Date());
        d.setUpdateTime(new Date());
        return d;
    }

    private IngestDraftVo toDraftVo(KbIngestDraft d) {
        IngestDraftVo vo = new IngestDraftVo();
        vo.setId(d.getId());
        vo.setJobId(d.getJobId());
        vo.setSlug(d.getSlug());
        vo.setDisplaySlug(bareSlug(d.getSlug()));
        vo.setKbType(d.getKbType());
        vo.setAction(d.getAction());
        vo.setBaseline(d.getBaseline());
        vo.setPatch(d.getPatch());
        vo.setDraft(resolveDraftContent(d));
        vo.setApproval(d.getApproval());
        vo.setUpdateTime(d.getUpdateTime());
        return vo;
    }

    private KbIngestDraft loadDraft(Long jobId, String slug) {
        if (StringUtils.isBlank(slug)) {
            throw new BaseException("slug 不能为空");
        }
        KbIngestDraft d = draftMapper.selectOne(new LambdaQueryWrapper<KbIngestDraft>()
                .eq(KbIngestDraft::getJobId, jobId)
                .eq(KbIngestDraft::getSlug, slug)
                .last("limit 1"));
        if (d == null) {
            throw new BaseException("草稿不存在: " + slug);
        }
        return d;
    }

    private JSONObject findPlanItem(Long jobId, String slug, String action) {
        KbIngestPlan plan = latestPlan(jobId);
        if (plan == null) {
            throw new BaseException("Plan 不存在，无法重生成");
        }
        JSONObject obj = JSON.parseObject(plan.getPlanJson());
        String bare = bareSlug(slug);
        JSONArray arr = obj.getJSONArray(ACT_ENRICH.equals(action) ? "enrich" : "create");
        if (arr != null) {
            for (int i = 0; i < arr.size(); i++) {
                JSONObject it = arr.getJSONObject(i);
                if (bare.equalsIgnoreCase(bareSlug(StringUtils.trimToEmpty(it.getString("slug"))))) {
                    return it;
                }
            }
        }
        // 找不到对应 plan 项时给个兜底（用当前 slug）
        JSONObject fallback = new JSONObject();
        fallback.put("slug", bare);
        return fallback;
    }

    private List<String> collectPlanSlugs(JSONArray create, JSONArray enrich) {
        List<String> out = new ArrayList<>();
        if (create != null) {
            for (int i = 0; i < create.size(); i++) {
                String s = create.getJSONObject(i).getString("slug");
                if (StringUtils.isNotBlank(s)) {
                    out.add(bareSlug(s).toLowerCase(Locale.ROOT));
                }
            }
        }
        if (enrich != null) {
            for (int i = 0; i < enrich.size(); i++) {
                String s = enrich.getJSONObject(i).getString("slug");
                if (StringUtils.isNotBlank(s)) {
                    out.add(bareSlug(s).toLowerCase(Locale.ROOT));
                }
            }
        }
        return out;
    }

    private List<String> candidateBareSlugs(Long spaceId) {
        LambdaQueryWrapper<KbDocument> w = new LambdaQueryWrapper<>();
        w.eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE);
        w.eq(KbDocument::getSpaceId, spaceId);
        w.select(KbDocument::getSlug);
        List<String> out = new ArrayList<>();
        for (KbDocument d : kbDocumentMapper.selectList(w)) {
            if (StringUtils.isNotBlank(d.getSlug())) {
                out.add(bareSlug(d.getSlug()));
            }
        }
        return out.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 供 PageWriter 互链的候选 slug：同批次 + 按主题召回（≤25），避免把全库 slug 喂给 LLM 导致 related 泛滥。
     */
    private List<String> linkCandidateBareSlugs(Long spaceId, String topic, List<String> batchFullSlugs) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String s : batchBareNames(batchFullSlugs)) {
            set.add(s);
        }
        if (StringUtils.isNotBlank(topic)) {
            try {
                List<KbDocument> docs = kbDocumentMapper.searchAskCandidates(
                        java.util.Collections.singletonList(spaceId),
                        DocumentStatus.PUBLISHED.getCode(),
                        null, null, topic, 15);
                if (docs != null) {
                    for (KbDocument d : docs) {
                        if (StringUtils.isNotBlank(d.getSlug())) {
                            set.add(bareSlug(d.getSlug()));
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("[ingest] linkCandidateBareSlugs 召回失败: {}", e.getMessage());
            }
        }
        return limit(new ArrayList<>(set), MAX_LINK_CANDIDATES);
    }

    private List<String> batchBareNames(List<String> batchFullSlugs) {
        if (batchFullSlugs == null) {
            return new ArrayList<>();
        }
        return batchFullSlugs.stream()
                .map(this::bareSlug)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /** 落盘前清洗 related：仅保留正文 [[..]]、planRelated、同批次页，最多 5 个。 */
    private String sanitizeRelatedFrontmatter(String content, String currentBare,
                                            List<String> batchBareSlugs, List<String> planRelated) {
        if (!content.startsWith("---")) {
            return content;
        }
        int end = content.indexOf("\n---", 3);
        if (end < 0) {
            return content;
        }
        String body = content.substring(end + 4);
        LinkedHashSet<String> kept = new LinkedHashSet<>();
        for (String t : extractWikilinkTargets(body)) {
            kept.add(bareSlug(t));
        }
        if (planRelated != null) {
            for (String r : planRelated) {
                if (StringUtils.isNotBlank(r)) {
                    kept.add(bareSlug(r));
                }
            }
        }
        if (batchBareSlugs != null) {
            for (String b : batchBareSlugs) {
                if (StringUtils.isNotBlank(b) && !bareSlug(b).equalsIgnoreCase(currentBare)) {
                    kept.add(bareSlug(b));
                }
            }
        }
        kept.remove(currentBare);
        List<String> list = limit(new ArrayList<>(kept), MAX_RELATED_SLUGS);
        return setFrontmatterRelated(content, list);
    }

    private List<String> extractWikilinkTargets(String body) {
        List<String> out = new ArrayList<>();
        Matcher m = WIKILINK.matcher(body);
        while (m.find()) {
            String t = m.group(1).split("\\|")[0].trim();
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return out;
    }

    private String setFrontmatterRelated(String content, List<String> relatedBare) {
        int end = content.indexOf("\n---", 3);
        if (end < 0) {
            return content;
        }
        String fm = content.substring(0, end);
        String rest = content.substring(end);
        String cleaned = fm.replaceAll("(?m)^related:\\s*\\[[^\\]]*]\\s*\\n?", "");
        cleaned = cleaned.replaceAll("(?m)^related:\\s*\\n(?:  - [^\\n]+\\n)+", "");
        cleaned = cleaned.replaceAll("(?m)^related:\\s*\\n?", "");
        cleaned = StringUtils.stripEnd(cleaned, null);

        StringBuilder relatedBlock = new StringBuilder("\n");
        if (relatedBare == null || relatedBare.isEmpty()) {
            relatedBlock.append("related: []");
        } else if (relatedBare.size() <= 4 && relatedBare.stream().noneMatch(s -> s.contains(" "))) {
            relatedBlock.append("related: [").append(String.join(", ", relatedBare)).append(']');
        } else {
            relatedBlock.append("related:");
            for (String r : relatedBare) {
                relatedBlock.append("\n  - ").append(r);
            }
        }
        return cleaned + relatedBlock + rest;
    }

    private int countRelatedInFrontmatter(String content) {
        if (!content.startsWith("---")) {
            return 0;
        }
        int end = content.indexOf("\n---", 3);
        if (end < 0) {
            return 0;
        }
        String fm = content.substring(0, end);
        Matcher inline = RELATED_INLINE.matcher(fm);
        if (inline.find()) {
            String inner = inline.group(1).trim();
            if (inner.isEmpty()) {
                return 0;
            }
            return inner.split(",").length;
        }
        int count = 0;
        int idx = fm.indexOf("related:");
        if (idx < 0) {
            return 0;
        }
        String block = fm.substring(idx);
        Matcher bm = Pattern.compile("^  - ", Pattern.MULTILINE).matcher(block);
        while (bm.find()) {
            count++;
        }
        return count;
    }

    private void indexDocuments(Long spaceId, Set<String> slugIndex, Set<String> titleIndex) {
        LambdaQueryWrapper<KbDocument> w = new LambdaQueryWrapper<>();
        w.eq(KbDocument::getIsDelete, CommonConstant.UN_DELETE);
        w.eq(KbDocument::getSpaceId, spaceId);
        w.select(KbDocument::getSlug, KbDocument::getTitle);
        for (KbDocument d : kbDocumentMapper.selectList(w)) {
            if (StringUtils.isNotBlank(d.getSlug())) {
                slugIndex.add(bareSlug(d.getSlug()).toLowerCase(Locale.ROOT));
            }
            if (StringUtils.isNotBlank(d.getTitle())) {
                titleIndex.add(d.getTitle().trim().toLowerCase(Locale.ROOT));
            }
        }
    }

    private boolean resolvesLink(String target, Set<String> slugIndex, Set<String> titleIndex) {
        String t = target.toLowerCase(Locale.ROOT);
        int slash = t.lastIndexOf('/');
        if (slash >= 0) {
            t = t.substring(slash + 1);
        }
        return slugIndex.contains(t) || titleIndex.contains(t);
    }

    private String typeDir(String type) {
        if (StringUtils.isBlank(type)) {
            return "articles";
        }
        return TYPE_DIRS.getOrDefault(type.trim().toLowerCase(Locale.ROOT), "articles");
    }

    private String inferTypeFromRelPath(String relPath) {
        if (StringUtils.isBlank(relPath) || !relPath.contains("/")) {
            return "article";
        }
        String dir = relPath.substring(0, relPath.indexOf('/'));
        for (Map.Entry<String, String> e : TYPE_DIRS.entrySet()) {
            if (e.getValue().equals(dir)) {
                return e.getKey();
            }
        }
        return "article";
    }

    private String bareSlug(String relPath) {
        if (StringUtils.isBlank(relPath)) {
            return "";
        }
        String s = relPath.trim().replace('\\', '/');
        int slash = s.lastIndexOf('/');
        s = slash >= 0 ? s.substring(slash + 1) : s;
        if (s.endsWith(".md")) {
            s = s.substring(0, s.length() - 3);
        }
        return s;
    }

    /** 在 wiki 目录下按 planSlug 找已有页相对路径；找不到返回 null。 */
    private String resolveExistingRelPath(String spaceCode, String planSlug) {
        String wikiDir = resolveWikiDir(spaceCode);
        Path base = resolveWikiBase(spaceCode);
        String s = planSlug.trim().replace('\\', '/');
        if (s.endsWith(".md")) {
            s = s.substring(0, s.length() - 3);
        }
        if (s.contains("/")) {
            Path f = base.resolve(s + ".md").normalize();
            if (f.startsWith(base) && Files.exists(f)) {
                return s;
            }
        }
        String bare = bareSlug(s);
        for (String dir : TYPE_DIRS.values()) {
            Path f = base.resolve(dir + "/" + bare + ".md").normalize();
            if (f.startsWith(base) && Files.exists(f)) {
                return dir + "/" + bare;
            }
        }
        return null;
    }

    private String readWikiFile(String spaceCode, String relPath) {
        Path base = resolveWikiBase(spaceCode);
        Path f = base.resolve(relPath + ".md").normalize();
        if (!f.startsWith(base) || !Files.exists(f)) {
            return "";
        }
        try {
            return new String(Files.readAllBytes(f), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    /** 读 plan sources 引用的 raw 文件内容（截断拼接）。 */
    private String readSources(List<String> sources) {
        if (sources == null || sources.isEmpty()) {
            return "（无 sources，按主题与已知上下文撰写）";
        }
        Path rawRoot = resolveRawRoot();
        StringBuilder sb = new StringBuilder();
        for (String src : sources) {
            String rel = stripRawPrefix(src);
            Path f;
            try {
                f = rawRoot.resolve(rel).normalize();
            } catch (Exception e) {
                continue;
            }
            sb.append("\n===== ").append(src).append(" =====\n");
            if (f.startsWith(rawRoot) && Files.isRegularFile(f)) {
                sb.append(readSnippet(f));
            } else {
                sb.append("（源文件未找到，跳过）");
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private String stripRawPrefix(String src) {
        String s = src.trim().replace('\\', '/');
        int idx = s.indexOf("raw/");
        if (idx >= 0) {
            return s.substring(idx + 4);
        }
        return s;
    }

    private String stripFence(String text) {
        if (text == null) {
            return "";
        }
        String s = text.trim();
        if (s.startsWith("```")) {
            int nl = s.indexOf('\n');
            if (nl > 0) {
                s = s.substring(nl + 1);
            }
            if (s.endsWith("```")) {
                s = s.substring(0, s.length() - 3).trim();
            }
        }
        return s;
    }

    private List<String> jsonStrList(JSONArray arr) {
        List<String> out = new ArrayList<>();
        if (arr != null) {
            for (int i = 0; i < arr.size(); i++) {
                String s = arr.getString(i);
                if (StringUtils.isNotBlank(s)) {
                    out.add(s.trim());
                }
            }
        }
        return out;
    }

    private String bulletList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "（无）\n";
        }
        StringBuilder sb = new StringBuilder();
        for (String s : items) {
            sb.append("- ").append(s).append('\n');
        }
        return sb.toString();
    }

    private List<String> limit(List<String> list, int n) {
        if (list == null) {
            return new ArrayList<>();
        }
        return list.stream().distinct().limit(n).collect(Collectors.toList());
    }

    private String resolveWikiDir(String spaceCode) {
        String dir = wikiProperties.getSpaceDirs().get(spaceCode);
        if (StringUtils.isBlank(dir)) {
            throw new BaseException("空间未配置 wiki 目录: " + spaceCode);
        }
        return dir;
    }

    private Path resolveWikiBase(String spaceCode) {
        Path root = Paths.get(wikiProperties.getRoot());
        if (!root.isAbsolute()) {
            root = Paths.get(System.getProperty("user.dir")).resolve(root);
        }
        return root.resolve(resolveWikiDir(spaceCode)).normalize();
    }

    private Path resolveWikiRelFile(String spaceCode, String relFile) {
        Path base = resolveWikiBase(spaceCode);
        Path f = base.resolve(relFile).normalize();
        if (!f.startsWith(base)) {
            throw new BaseException("非法路径（越权）: " + relFile);
        }
        return f;
    }

    /** enrich：baseline + patch 合并为落盘/预览全文。 */
    private String mergeEnrich(String baseline, String patch) {
        if (StringUtils.isBlank(baseline)) {
            return StringUtils.defaultString(patch);
        }
        if (StringUtils.isBlank(patch)) {
            return baseline;
        }
        return baseline.replaceAll("\\s+$", "") + "\n\n" + patch + "\n";
    }

    /** 解析草稿落盘/ lint 用全文（优先 draft，否则 baseline+patch）。 */
    private String resolveDraftContent(KbIngestDraft d) {
        if (d == null) {
            return "";
        }
        if (StringUtils.isNotBlank(d.getDraft())) {
            return d.getDraft();
        }
        if (ACT_ENRICH.equals(d.getAction())) {
            return mergeEnrich(StringUtils.defaultString(d.getBaseline()), d.getPatch());
        }
        return "";
    }

    // ---------------------------------------------------------------- T15e 模板

    @Override
    public List<IngestTemplateVo> listTemplates(Long spaceId) {
        assertEnabled();
        List<Long> accessible = kbAclService.accessibleSpaceIds();
        if (accessible.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<KbIngestTemplate> w = new LambdaQueryWrapper<>();
        w.eq(KbIngestTemplate::getIsDelete, CommonConstant.UN_DELETE);
        w.in(KbIngestTemplate::getSpaceId, accessible);
        if (spaceId != null) {
            w.eq(KbIngestTemplate::getSpaceId, spaceId);
        }
        w.orderByDesc(KbIngestTemplate::getCreateTime);
        List<KbIngestTemplate> tpls = templateMapper.selectList(w);
        Map<Long, KbSpace> spaceMap = loadSpaceMap(tpls.stream()
                .map(KbIngestTemplate::getSpaceId).collect(Collectors.toList()));
        List<IngestTemplateVo> out = new ArrayList<>();
        for (KbIngestTemplate tpl : tpls) {
            out.add(toTemplateVo(tpl, spaceMap.get(tpl.getSpaceId())));
        }
        return out;
    }

    /** 批量按 id 加载空间，去重，避免逐条 selectById（N+1）。 */
    private Map<Long, KbSpace> loadSpaceMap(List<Long> spaceIds) {
        List<Long> distinct = spaceIds.stream().filter(java.util.Objects::nonNull)
                .distinct().collect(Collectors.toList());
        if (distinct.isEmpty()) {
            return new java.util.HashMap<>();
        }
        return kbSpaceMapper.selectBatchIds(distinct).stream()
                .collect(Collectors.toMap(KbSpace::getId, s -> s, (a, b) -> a));
    }

    @Override
    public IngestTemplateVo createTemplate(IngestTemplateCreateRequest request) {
        assertEnabled();
        if (request == null || StringUtils.isBlank(request.getName()) || StringUtils.isBlank(request.getTopic())) {
            throw new BaseException("模板名称与主题不能为空");
        }
        KbSpace space = resolveSpace(request.getSpaceId());
        kbAclService.assertCanEdit(space.getId());

        List<String> rawPaths = request.getRawPaths();
        if (rawPaths != null && !rawPaths.isEmpty()) {
            Path root = resolveRawRoot();
            for (String rp : rawPaths) {
                Path target = normalizeUnder(root, rp);
                if (!Files.exists(target)) {
                    throw new BaseException("raw 路径不存在: " + rp);
                }
            }
        }

        KbIngestTemplate tpl = new KbIngestTemplate();
        tpl.setId(IdGenerator.getId());
        tpl.setSpaceId(space.getId());
        tpl.setName(request.getName().trim());
        tpl.setTopic(request.getTopic().trim());
        tpl.setExpectTypes(StringUtils.trimToNull(request.getExpectTypes()));
        tpl.setRawPaths(rawPaths == null || rawPaths.isEmpty() ? "[]" : JSON.toJSONString(rawPaths));
        tpl.setPlanJson(StringUtils.trimToNull(request.getPlanJson()));
        Long uid = ShiroUtils.getUserId();
        tpl.setCreateId(uid);
        tpl.setCreateTime(new Date());
        tpl.setUpdateId(uid);
        tpl.setUpdateTime(new Date());
        tpl.setIsDelete(CommonConstant.UN_DELETE);
        templateMapper.insert(tpl);
        return toTemplateVo(tpl);
    }

    @Override
    public IngestTemplateVo saveJobAsTemplate(Long jobId, IngestSaveAsTemplateRequest request) {
        assertEnabled();
        if (request == null || StringUtils.isBlank(request.getName())) {
            throw new BaseException("模板名称不能为空");
        }
        KbIngestJob job = loadJob(jobId);
        kbAclService.assertCanEdit(job.getSpaceId());

        boolean includePlan = request.getIncludePlan() == null || Boolean.TRUE.equals(request.getIncludePlan());
        String planJson = null;
        if (includePlan) {
            KbIngestPlan plan = latestPlan(jobId);
            if (plan != null && StringUtils.isNotBlank(plan.getPlanJson())) {
                planJson = plan.getPlanJson();
            }
        }

        IngestTemplateCreateRequest req = new IngestTemplateCreateRequest();
        req.setSpaceId(job.getSpaceId());
        req.setName(request.getName().trim());
        req.setTopic(job.getTopic());
        req.setExpectTypes(job.getExpectTypes());
        req.setRawPaths(parsePaths(job.getRawPaths()));
        req.setPlanJson(planJson);
        return createTemplate(req);
    }

    @Override
    public void deleteTemplate(Long id) {
        assertEnabled();
        if (id == null) {
            throw new BaseException("模板ID不能为空");
        }
        KbIngestTemplate tpl = templateMapper.selectById(id);
        if (tpl == null || (tpl.getIsDelete() != null && tpl.getIsDelete() == 1)) {
            throw new BaseException("模板不存在");
        }
        kbAclService.assertCanEdit(tpl.getSpaceId());
        tpl.setIsDelete(1);
        tpl.setUpdateId(ShiroUtils.getUserId());
        tpl.setUpdateTime(new Date());
        templateMapper.updateById(tpl);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IngestJobVo createJobFromTemplate(Long templateId, IngestJobFromTemplateRequest request) {
        assertEnabled();
        if (templateId == null) {
            throw new BaseException("模板ID不能为空");
        }
        KbIngestTemplate tpl = templateMapper.selectById(templateId);
        if (tpl == null || (tpl.getIsDelete() != null && tpl.getIsDelete() == 1)) {
            throw new BaseException("模板不存在");
        }
        kbAclService.assertCanEdit(tpl.getSpaceId());

        IngestJobCreateRequest jobReq = new IngestJobCreateRequest();
        jobReq.setSpaceId(tpl.getSpaceId());
        jobReq.setTopic(request != null && StringUtils.isNotBlank(request.getTopic())
                ? request.getTopic().trim() : tpl.getTopic());
        jobReq.setBatchNo(request != null ? request.getBatchNo() : null);
        jobReq.setExpectTypes(tpl.getExpectTypes());
        jobReq.setRawPaths(parsePaths(tpl.getRawPaths()));
        if (jobReq.getRawPaths().isEmpty()) {
            throw new BaseException("模板 raw 源为空");
        }
        IngestJobVo jobVo = createJob(jobReq);

        if (StringUtils.isNotBlank(tpl.getPlanJson())) {
            KbIngestPlan plan = new KbIngestPlan();
            plan.setId(IdGenerator.getId());
            plan.setJobId(jobVo.getId());
            plan.setVersion(1);
            plan.setPlanJson(tpl.getPlanJson());
            plan.setSource("template");
            plan.setCreateId(ShiroUtils.getUserId());
            plan.setCreateTime(new Date());
            planMapper.insert(plan);

            KbIngestJob job = loadJob(jobVo.getId());
            job.setPlanVersion(1);
            job.setStatus(ST_PLANNED);
            job.setUpdateId(ShiroUtils.getUserId());
            job.setUpdateTime(new Date());
            jobMapper.updateById(job);
            return getJob(jobVo.getId());
        }
        return jobVo;
    }

    private IngestTemplateVo toTemplateVo(KbIngestTemplate tpl) {
        return toTemplateVo(tpl, kbSpaceMapper.selectById(tpl.getSpaceId()));
    }

    private IngestTemplateVo toTemplateVo(KbIngestTemplate tpl, KbSpace space) {
        IngestTemplateVo vo = new IngestTemplateVo();
        vo.setId(tpl.getId());
        vo.setSpaceId(tpl.getSpaceId());
        vo.setSpaceCode(space == null ? null : space.getSpaceCode());
        vo.setName(tpl.getName());
        vo.setTopic(tpl.getTopic());
        vo.setExpectTypes(tpl.getExpectTypes());
        vo.setRawPaths(parsePaths(tpl.getRawPaths()));
        vo.setHasPlan(StringUtils.isNotBlank(tpl.getPlanJson()));
        vo.setCreateTime(tpl.getCreateTime());
        return vo;
    }
}
