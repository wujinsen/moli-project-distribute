package com.moli.user.center.server.config;

import com.alibaba.druid.pool.DruidDataSource;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.function.ToDoubleFunction;

/**
 * Exposes Druid JDBC pool gauges to Prometheus ({@code /actuator/prometheus}).
 * <p>
 * Metrics: {@code druid.pool.active|max|waiting|peak}
 */
@Configuration
@ConditionalOnClass(DruidDataSource.class)
public class DruidPoolMetricsConfiguration {

    @Bean
    public MeterBinder druidPoolMetrics(DataSource dataSource) {
        return registry -> bindDruidPool(registry, dataSource);
    }

    static void bindDruidPool(MeterRegistry registry, DataSource dataSource) {
        if (!(dataSource instanceof DruidDataSource)) {
            return;
        }
        DruidDataSource ds = (DruidDataSource) dataSource;
        registerGauge(registry, "druid.pool.active", ds, DruidDataSource::getActiveCount);
        registerGauge(registry, "druid.pool.max", ds, DruidDataSource::getMaxActive);
        registerGauge(registry, "druid.pool.waiting", ds, DruidDataSource::getWaitThreadCount);
        registerGauge(registry, "druid.pool.peak", ds, DruidDataSource::getActivePeak);
        registerGauge(registry, "druid.pool.idle", ds, DruidDataSource::getPoolingCount);
        registerGauge(registry, "druid.pool.create_error", ds, DruidDataSource::getCreateErrorCount);
    }

    private static void registerGauge(MeterRegistry registry, String name, DruidDataSource ds,
                                      ToDoubleFunction<DruidDataSource> fn) {
        Gauge.builder(name, ds, fn).description("Druid JDBC pool metric").register(registry);
    }
}
