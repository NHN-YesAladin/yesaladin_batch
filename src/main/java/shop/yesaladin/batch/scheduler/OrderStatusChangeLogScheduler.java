package shop.yesaladin.batch.scheduler;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 주문의 상태 변경 이력을 조회하고 추가하는 Job 의 스케줄러 입니다.
 *
 * @author 이수정
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OrderStatusChangeLogScheduler {

    private final Job insertOrderStatusChangeLogJob;
    private final JobLauncher jobLauncher;
    private static final String EVERY_MIDNIGHT = "0 0 0 * * *";

    @Scheduled(cron = EVERY_MIDNIGHT, zone = "Asia/Seoul")
    public void scheduleInsertOrderStatusChangeLog() {
        try {
            log.info("start time = {}", LocalDateTime.now());

            LocalDate threeDaysAgo = LocalDate.now().minusDays(3);
            log.info("threeDaysAgoDate = {}", threeDaysAgo);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("threeDaysAgoDate", threeDaysAgo.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .addDate("currentTime", new Date())
                    .toJobParameters();

            jobLauncher.run(insertOrderStatusChangeLogJob, jobParameters);
        } catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException |
                 JobParametersInvalidException | JobRestartException e) {
            log.error(e.getMessage());
        }
    }
}
