package com.gdoc.common.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

@Component
public class HealthCheck implements HealthIndicator {

    @Override
    public Health health() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long used = memoryBean.getHeapMemoryUsage().getUsed();
        long max = memoryBean.getHeapMemoryUsage().getMax();
        double usagePercent = max > 0 ? (double) used / max * 100 : 0;

        Health.Builder builder = usagePercent < 90 ? Health.up() : Health.down();
        builder.withDetail("heapUsedMB", used / 1024 / 1024)
               .withDetail("heapMaxMB", max / 1024 / 1024)
               .withDetail("heapUsagePercent", String.format("%.1f%%", usagePercent))
               .withDetail("threads", Thread.activeCount())
               .withDetail("uptime", ManagementFactory.getRuntimeMXBean().getUptime() / 1000 + "s");

        return builder.build();
    }
}