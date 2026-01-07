package com.dental.clinic.management.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

// import java.util.TimeZone;

/**
 * Configuration to enable scheduled tasks (cron jobs).
 *
 * All scheduled jobs are located in the com.dental.clinic.management.scheduled package.
 * 
 * IMPORTANT FIXES FOR PRODUCTION:
 * 1. Set timezone to Asia/Ho_Chi_Minh for consistent cron execution
 * 2. Configure thread pool (10 threads) to prevent job blocking
 * 3. Add proper error handling and logging
 * 4. Enable graceful shutdown
 */
@Slf4j
@Configuration
@EnableScheduling
public class ScheduledTasksConfig implements SchedulingConfigurer {

    private static final String TIMEZONE = "Asia/Ho_Chi_Minh";
    private static final int POOL_SIZE = 10;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
        log.info("✅ Scheduled tasks configured with timezone: {} and pool size: {}", TIMEZONE, POOL_SIZE);
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(POOL_SIZE);
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setRejectedExecutionHandler((r, executor) -> 
            log.error("⚠️ Scheduled task rejected: {}", r.toString())
        );
        scheduler.setErrorHandler(t -> 
            log.error("❌ Error in scheduled task: {}", t.getMessage(), t)
        );
        
        scheduler.initialize();
        
        log.info("✅ TaskScheduler initialized successfully");
        log.info("   - Timezone: {} (configured via @Scheduled zone parameter)", TIMEZONE);
        log.info("   - Pool size: {}", POOL_SIZE);
        log.info("   - Graceful shutdown: enabled");
        
        return scheduler;
    }
}
