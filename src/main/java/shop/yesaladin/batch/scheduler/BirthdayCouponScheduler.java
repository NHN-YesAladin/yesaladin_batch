package shop.yesaladin.batch.scheduler;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 생일 쿠폰 지급 Job 의 스케줄러 입니다.
 *
 * @author 서민지
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BirthdayCouponScheduler {

    private final Job giveBirthdayCouponJob;
    private final JobLauncher jobLauncher;
    private static final String ONE_AM_EVERY_DAY = "0 0 1 * * *";
    private static final int LATER_DAYS = 7;

    /**
     * 매월 01시에 laterDays 파라미터를 갖는 giveBirthdayCouponJob 을 실행합니다.
     */
    @Scheduled(cron = ONE_AM_EVERY_DAY, zone = "Asia/Seoul")
    public void scheduleGiveBirthdayCoupon() {
        log.info("=== giveBirthdayCoupon schedule started at {} ===", LocalDateTime.now());

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("laterDays", String.valueOf(LATER_DAYS))
                .addDate("currentDate", new Date())
                .toJobParameters();

        try {
            jobLauncher.run(giveBirthdayCouponJob, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException | JobRestartException e) {
            log.error(e.getMessage());
        }

        log.info("=== giveBirthdayCoupon schedule ended at {} ===", LocalDateTime.now());
    }
}
