package com.moli.user.center.server.operation.support;

import com.moli.user.center.server.operation.config.OperationHealthProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 探活专用有界线程池（TCP 并行探测，与 ops.task 异步任务池隔离）。
 */
@Component
public class OperationHealthProbeExecutor {

    private final OperationHealthProperties healthProperties;
    private ExecutorService executor;

    public OperationHealthProbeExecutor(OperationHealthProperties healthProperties) {
        this.healthProperties = healthProperties;
    }

    @PostConstruct
    public void init() {
        int size = Math.max(1, healthProperties.getProbeParallelism());
        ThreadFactory factory = new ThreadFactory() {
            private final AtomicInteger seq = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "ops-health-probe-" + seq.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        };
        executor = Executors.newFixedThreadPool(size, factory);
    }

    @PreDestroy
    public void shutdown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
