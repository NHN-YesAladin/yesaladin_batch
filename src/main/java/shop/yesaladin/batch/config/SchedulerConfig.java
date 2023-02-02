package shop.yesaladin.batch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Spring Scheduler 설정 입니다.
 *
 * @author 서민지
 * @since 1.0
 */
@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

    private static final int SCHEDULE_THREAD_SIZE = 5;

    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(SCHEDULE_THREAD_SIZE);
        threadPoolTaskScheduler.setThreadNamePrefix("SCHEDULER-");
        threadPoolTaskScheduler.initialize();

        return threadPoolTaskScheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(threadPoolTaskScheduler());
    }
}
