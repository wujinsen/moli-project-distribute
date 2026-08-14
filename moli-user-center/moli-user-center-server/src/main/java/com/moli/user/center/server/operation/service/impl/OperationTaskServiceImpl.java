package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.exception.BaseException;
import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationTask;
import com.moli.user.center.common.domain.vo.OperationTaskProjectGroupVo;
import com.moli.user.center.common.domain.vo.OperationTaskVo;
import com.moli.user.center.server.operation.config.OperationTaskProperties;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationTaskMapper;
import com.moli.user.center.server.operation.service.OperationTaskService;
import com.moli.user.center.server.operation.task.OperationTaskContext;
import com.moli.user.center.server.operation.task.OperationTaskCancelledException;
import com.moli.user.center.server.operation.task.OperationTaskJob;
import com.moli.user.center.server.operation.task.OperationTaskStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 异步任务服务（SVR-14）：内存缓冲实时日志 + 节流落库，轮询按字符偏移取增量。
 */
@Service
public class OperationTaskServiceImpl implements OperationTaskService {

    private static final Logger log = LoggerFactory.getLogger(OperationTaskServiceImpl.class);
    private static final long DB_FLUSH_INTERVAL_MS = 2000;

    @Resource
    private OperationTaskMapper operationTaskMapper;
    @Resource
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Resource
    private OperationTaskProperties taskProperties;

    private ExecutorService executor;
    /** 运行中任务的实时上下文：taskId → context。 */
    private final Map<Long, LiveContext> liveTasks = new ConcurrentHashMap<>();
    /** 互斥锁：lockKey → taskId。 */
    private final Map<String, Long> runningLocks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        ThreadFactory factory = new ThreadFactory() {
            private final AtomicInteger seq = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "ops-task-" + seq.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        };
        executor = Executors.newFixedThreadPool(Math.max(1, taskProperties.getPoolSize()), factory);
    }

    @PreDestroy
    public void shutdown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Override
    public OperationTask create(String taskType, Long serverId, Long projectId,
                                String serviceKey, String action, String targetName) {
        OperationTask task = new OperationTask();
        task.setTaskType(taskType);
        task.setServerId(serverId);
        task.setProjectId(projectId);
        task.setServiceKey(serviceKey);
        task.setAction(action);
        task.setTargetName(StringUtils.abbreviate(targetName, 250));
        task.setStatus(OperationTaskStatus.PENDING);
        task.setProgress(0);
        operationTaskMapper.insert(task);
        return task;
    }

    @Override
    public void submit(Long taskId, String lockKey, OperationTaskJob job) {
        OperationTask task = requireTask(taskId);
        if (!OperationTaskStatus.PENDING.equals(task.getStatus())) {
            throw new BaseException("任务已提交，勿重复执行");
        }
        if (StringUtils.isNotBlank(lockKey)) {
            Long holder = runningLocks.putIfAbsent(lockKey, taskId);
            if (holder != null) {
                markFailed(taskId, "同一目标已有任务 #" + holder + " 在执行，请稍后再试");
                throw new BaseException("同一目标已有任务在执行（#" + holder + "），请等待其完成");
            }
        }
        LiveContext context = new LiveContext(taskId);
        liveTasks.put(taskId, context);
        try {
            executor.execute(() -> runJob(taskId, lockKey, context, job));
        } catch (Exception e) {
            cleanup(taskId, lockKey);
            markFailed(taskId, "任务提交失败: " + e.getMessage());
            throw new BaseException("任务提交失败: " + e.getMessage());
        }
    }

    private void runJob(Long taskId, String lockKey, LiveContext context, OperationTaskJob job) {
        if (context.isCancelled()) {
            finishCancelled(taskId, context);
            cleanup(taskId, lockKey);
            return;
        }
        updateStatus(taskId, OperationTaskStatus.RUNNING, null);
        context.status = OperationTaskStatus.RUNNING;
        try {
            job.run(context);
            if (context.isCancelled()) {
                finishCancelled(taskId, context);
            } else {
                context.status = OperationTaskStatus.SUCCESS;
                context.progress = 100;
                finish(taskId, context, OperationTaskStatus.SUCCESS, "执行成功");
            }
        } catch (OperationTaskCancelledException e) {
            finishCancelled(taskId, context);
        } catch (Exception e) {
            log.warn("operation task {} failed", taskId, e);
            if (context.isCancelled()) {
                finishCancelled(taskId, context);
            } else {
                context.appendLine("[ERROR] " + e.getMessage());
                context.status = OperationTaskStatus.FAILED;
                finish(taskId, context, OperationTaskStatus.FAILED, StringUtils.abbreviate(e.getMessage(), 500));
            }
        } finally {
            cleanup(taskId, lockKey);
        }
    }

    private void finishCancelled(Long taskId, LiveContext context) {
        context.appendLine("[CANCEL] 任务已取消");
        context.status = OperationTaskStatus.CANCELLED;
        finish(taskId, context, OperationTaskStatus.CANCELLED, "用户取消");
    }

    private static boolean isTerminalStatus(String status) {
        return OperationTaskStatus.SUCCESS.equals(status)
                || OperationTaskStatus.FAILED.equals(status)
                || OperationTaskStatus.CANCELLED.equals(status);
    }

    private void cleanup(Long taskId, String lockKey) {
        if (StringUtils.isNotBlank(lockKey)) {
            runningLocks.remove(lockKey, taskId);
        }
        liveTasks.remove(taskId);
    }

    private void finish(Long taskId, LiveContext context, String status, String message) {
        OperationTask row = new OperationTask();
        row.setId(taskId);
        row.setStatus(status);
        row.setProgress(context.progress);
        row.setTaskLog(context.logSnapshot());
        row.setMessage(message);
        row.setFinishTime(new Date());
        operationTaskMapper.updateById(row);
    }

    private void markFailed(Long taskId, String message) {
        OperationTask row = new OperationTask();
        row.setId(taskId);
        row.setStatus(OperationTaskStatus.FAILED);
        row.setMessage(StringUtils.abbreviate(message, 500));
        row.setFinishTime(new Date());
        operationTaskMapper.updateById(row);
    }

    private void updateStatus(Long taskId, String status, String message) {
        OperationTask row = new OperationTask();
        row.setId(taskId);
        row.setStatus(status);
        row.setMessage(message);
        operationTaskMapper.updateById(row);
    }

    @Override
    public OperationTaskVo cancel(Long taskId) {
        OperationTask task = requireTask(taskId);
        if (isTerminalStatus(task.getStatus())) {
            throw new BaseException("任务已结束，无法取消");
        }
        LiveContext live = liveTasks.get(taskId);
        if (live != null) {
            live.requestCancel();
            live.appendLine("[CANCEL] 用户请求取消任务");
        } else if (OperationTaskStatus.PENDING.equals(task.getStatus())
                || OperationTaskStatus.RUNNING.equals(task.getStatus())) {
            markCancelled(taskId, "用户取消");
        }
        return poll(taskId, 0);
    }

    private void markCancelled(Long taskId, String message) {
        OperationTask row = new OperationTask();
        row.setId(taskId);
        row.setStatus(OperationTaskStatus.CANCELLED);
        row.setMessage(StringUtils.abbreviate(message, 500));
        row.setFinishTime(new Date());
        operationTaskMapper.updateById(row);
    }

    @Override
    public OperationTaskVo poll(Long taskId, int logOffset) {
        LiveContext live = liveTasks.get(taskId);
        OperationTask task = requireTask(taskId);
        OperationTaskVo vo = new OperationTaskVo();
        vo.setId(task.getId());
        vo.setTaskType(task.getTaskType());
        vo.setServerId(task.getServerId());
        vo.setProjectId(task.getProjectId());
        vo.setServiceKey(task.getServiceKey());
        vo.setAction(task.getAction());
        vo.setTargetName(task.getTargetName());
        vo.setCreateTime(task.getCreateTime());
        vo.setFinishTime(task.getFinishTime());

        String fullLog;
        if (live != null) {
            vo.setStatus(live.status);
            vo.setProgress(live.progress);
            vo.setMessage(task.getMessage());
            fullLog = live.logSnapshot();
        } else {
            vo.setStatus(task.getStatus());
            vo.setProgress(task.getProgress());
            vo.setMessage(task.getMessage());
            fullLog = StringUtils.defaultString(task.getTaskLog());
        }
        int offset = Math.max(0, logOffset);
        if (offset > fullLog.length()) {
            offset = fullLog.length();
        }
        vo.setLogChunk(fullLog.substring(offset));
        vo.setNextLogOffset(fullLog.length());
        vo.setFinished(isTerminalStatus(vo.getStatus()));
        return vo;
    }

    @Override
    public PageRes<OperationTaskVo> list(String taskType, Long serverId, Long projectId,
                                         Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<OperationTask> wrapper = buildListFilterWrapper(taskType, serverId, projectId, null);
        // 列表不携带大字段日志
        wrapper.select(OperationTask.class, f -> !"task_log".equals(f.getColumn()));
        wrapper.orderByDesc(OperationTask::getCreateTime);

        Page<OperationTask> page = new Page<>();
        page.setCurrent(pageNum == null ? 1 : pageNum);
        page.setSize(pageSize == null ? 10 : pageSize);
        operationTaskMapper.selectPage(page, wrapper);

        PageRes<OperationTaskVo> result = new PageRes<>();
        result.setTotal((int) page.getTotal());
        result.setPageNum((int) page.getCurrent());
        result.setPageSize((int) page.getSize());
        result.setList(page.getRecords().stream().map(this::toListVo).collect(Collectors.toList()));
        return result;
    }

    @Override
    public PageRes<OperationTaskProjectGroupVo> listGroups(String taskType, Long serverId, Long projectId,
                                                           String status, Integer pageNum, Integer pageSize,
                                                           Integer tasksPerGroup) {
        LambdaQueryWrapper<OperationTask> wrapper = buildListFilterWrapper(taskType, serverId, projectId, status);
        wrapper.select(OperationTask.class, f -> !"task_log".equals(f.getColumn()));
        wrapper.orderByDesc(OperationTask::getCreateTime);

        List<OperationTask> allTasks = operationTaskMapper.selectList(wrapper);
        Map<Long, List<OperationTask>> grouped = new HashMap<>();
        for (OperationTask task : allTasks) {
            grouped.computeIfAbsent(task.getProjectId(), ignored -> new ArrayList<>()).add(task);
        }

        List<ProjectGroupBucket> buckets = new ArrayList<>();
        for (Map.Entry<Long, List<OperationTask>> entry : grouped.entrySet()) {
            List<OperationTask> tasks = entry.getValue();
            tasks.sort(Comparator.comparing(OperationTask::getCreateTime,
                    Comparator.nullsLast(Comparator.reverseOrder())));
            ProjectGroupBucket bucket = new ProjectGroupBucket();
            bucket.projectId = entry.getKey();
            bucket.tasks = tasks;
            bucket.latestCreateTime = tasks.stream()
                    .map(OperationTask::getCreateTime)
                    .filter(Objects::nonNull)
                    .max(Date::compareTo)
                    .orElse(null);
            bucket.taskCount = tasks.size();
            bucket.runningCount = (int) tasks.stream()
                    .filter(t -> OperationTaskStatus.PENDING.equals(t.getStatus())
                            || OperationTaskStatus.RUNNING.equals(t.getStatus()))
                    .count();
            bucket.failedCount = (int) tasks.stream()
                    .filter(t -> OperationTaskStatus.FAILED.equals(t.getStatus()))
                    .count();
            bucket.successCount = (int) tasks.stream()
                    .filter(t -> OperationTaskStatus.SUCCESS.equals(t.getStatus()))
                    .count();
            buckets.add(bucket);
        }

        buckets.sort(Comparator
                .comparing(ProjectGroupBucket::getLatestCreateTime,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ProjectGroupBucket::getProjectId,
                        Comparator.nullsLast(Comparator.reverseOrder())));

        int pn = pageNum == null ? 1 : pageNum;
        int ps = pageSize == null ? 10 : pageSize;
        int total = buckets.size();
        int from = Math.max(0, (pn - 1) * ps);
        List<ProjectGroupBucket> pageBuckets = from >= total
                ? new ArrayList<>()
                : buckets.subList(from, Math.min(from + ps, total));

        Map<Long, String> projectNames = loadProjectNames(pageBuckets);
        int perGroup = tasksPerGroup == null ? 20 : Math.max(1, tasksPerGroup);

        PageRes<OperationTaskProjectGroupVo> result = new PageRes<>();
        result.setTotal(total);
        result.setPageNum(pn);
        result.setPageSize(ps);
        result.setList(pageBuckets.stream().map(bucket -> toGroupVo(bucket, projectNames, perGroup))
                .collect(Collectors.toList()));
        return result;
    }

    private LambdaQueryWrapper<OperationTask> buildListFilterWrapper(String taskType, Long serverId,
                                                                     Long projectId, String status) {
        LambdaQueryWrapper<OperationTask> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(taskType)) {
            wrapper.eq(OperationTask::getTaskType, taskType);
        }
        if (serverId != null) {
            wrapper.eq(OperationTask::getServerId, serverId);
        }
        if (projectId != null) {
            wrapper.eq(OperationTask::getProjectId, projectId);
        }
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(OperationTask::getStatus, status);
        }
        return wrapper;
    }

    private Map<Long, String> loadProjectNames(List<ProjectGroupBucket> pageBuckets) {
        List<Long> projectIds = pageBuckets.stream()
                .map(ProjectGroupBucket::getProjectId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (projectIds.isEmpty()) {
            return new HashMap<>();
        }
        return operationProjectDeployInfoMapper.selectBatchIds(projectIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        OperationProjectDeployInfo::getId,
                        OperationProjectDeployInfo::getProjectName,
                        (a, b) -> a));
    }

    private OperationTaskProjectGroupVo toGroupVo(ProjectGroupBucket bucket, Map<Long, String> projectNames,
                                                  int tasksPerGroup) {
        OperationTaskProjectGroupVo vo = new OperationTaskProjectGroupVo();
        vo.setProjectId(bucket.projectId);
        vo.setProjectName(bucket.projectId == null ? null : projectNames.get(bucket.projectId));
        vo.setTaskCount(bucket.taskCount);
        vo.setRunningCount(bucket.runningCount);
        vo.setFailedCount(bucket.failedCount);
        vo.setSuccessCount(bucket.successCount);
        vo.setLatestCreateTime(bucket.latestCreateTime);
        vo.setTasks(bucket.tasks.stream()
                .limit(tasksPerGroup)
                .map(this::toListVo)
                .collect(Collectors.toList()));
        return vo;
    }

    private static final class ProjectGroupBucket {
        private Long projectId;
        private List<OperationTask> tasks;
        private Date latestCreateTime;
        private int taskCount;
        private int runningCount;
        private int failedCount;
        private int successCount;

        private Date getLatestCreateTime() {
            return latestCreateTime;
        }

        private Long getProjectId() {
            return projectId;
        }
    }

    private OperationTaskVo toListVo(OperationTask task) {
        OperationTaskVo vo = new OperationTaskVo();
        vo.setId(task.getId());
        vo.setTaskType(task.getTaskType());
        vo.setServerId(task.getServerId());
        vo.setProjectId(task.getProjectId());
        vo.setServiceKey(task.getServiceKey());
        vo.setAction(task.getAction());
        vo.setTargetName(task.getTargetName());
        vo.setStatus(task.getStatus());
        vo.setProgress(task.getProgress());
        vo.setMessage(task.getMessage());
        vo.setCreateTime(task.getCreateTime());
        vo.setFinishTime(task.getFinishTime());
        vo.setFinished(isTerminalStatus(task.getStatus()));
        return vo;
    }

    private OperationTask requireTask(Long id) {
        OperationTask task = operationTaskMapper.selectById(id);
        if (task == null) {
            throw new BaseException("任务不存在: " + id);
        }
        return task;
    }

    /**
     * 运行中任务的内存态：日志缓冲 + 节流落库。
     */
    private class LiveContext implements OperationTaskContext {

        private final Long taskId;
        private final StringBuilder buffer = new StringBuilder();
        private volatile int progress;
        private volatile String status = OperationTaskStatus.PENDING;
        private volatile boolean cancelRequested;
        private volatile long lastFlush;

        LiveContext(Long taskId) {
            this.taskId = taskId;
        }

        void requestCancel() {
            cancelRequested = true;
            status = OperationTaskStatus.CANCELLED;
        }

        @Override
        public boolean isCancelled() {
            return cancelRequested;
        }

        @Override
        public void throwIfCancelled() throws OperationTaskCancelledException {
            if (cancelRequested) {
                throw new OperationTaskCancelledException();
            }
        }

        @Override
        public void appendLine(String line) {
            synchronized (buffer) {
                buffer.append(line == null ? "" : line).append('\n');
                int max = taskProperties.getLogMaxChars();
                if (buffer.length() > max) {
                    buffer.delete(0, buffer.length() - max);
                }
            }
            flushThrottled();
        }

        @Override
        public void setProgress(int value) {
            this.progress = Math.max(0, Math.min(100, value));
            flushThrottled();
        }

        @Override
        public int getProgress() {
            return progress;
        }

        String logSnapshot() {
            synchronized (buffer) {
                return buffer.toString();
            }
        }

        private void flushThrottled() {
            long now = System.currentTimeMillis();
            if (now - lastFlush < DB_FLUSH_INTERVAL_MS) {
                return;
            }
            lastFlush = now;
            try {
                OperationTask row = new OperationTask();
                row.setId(taskId);
                row.setProgress(progress);
                row.setTaskLog(logSnapshot());
                operationTaskMapper.updateById(row);
            } catch (Exception e) {
                log.warn("flush task {} log failed: {}", taskId, e.getMessage());
            }
        }
    }
}
