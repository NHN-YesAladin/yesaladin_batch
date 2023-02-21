package shop.yesaladin.batch.order.scheduler;

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
 * 구독이 만료되기 1달 전부터 구독 갱신을 위한 알림을 보내는 Job 의 스케줄러 입니다.
 *
 * @author 이수정
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class RenewalOfSubscriptionNotifyScheduler {

    private final Job notifyRenewalOfSubscriptionJob;
    private final JobLauncher jobLauncher;
//    private static final String EVERY_DAY = "0 0 10 * * *";
    private static final String EVERY_DAY = "0 * * * * *";

    @Scheduled(cron = EVERY_DAY, zone = "Asia/Seoul")
    public void scheduleRenewalOfSubscriptionNotify() {
        try {
            log.info("start time = {}", LocalDateTime.now());

            LocalDate oneMonthLater = LocalDate.now().plusWeeks(4);
            LocalDate oneWeekLater = LocalDate.now().plusWeeks(1);
            LocalDate oneDayLater = LocalDate.now().plusDays(1);
            log.info("oneMonthLater = {}", oneMonthLater);
            log.info("oneWeekLater = {}", oneWeekLater);
            log.info("oneDayLater = {}", oneDayLater);

            notifyRenewalOfSubscription(oneMonthLater, "1달");
            notifyRenewalOfSubscription(oneWeekLater, "일주일");
            notifyRenewalOfSubscription(oneDayLater, "하루");
        } catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException |
                 JobParametersInvalidException | JobRestartException e) {
            log.error(String.valueOf(e));
        }
    }

    /**
     * 1달, 일주일, 하루마다 notifyRenewalOfSubscriptionJob 을 실행합니다.
     *
     * @param nextRenewalDate remainingDate 후 만료 예정일이 될 날짜
     * @param remainingDate   1달, 일주일, 하루
     * @throws JobExecutionAlreadyRunningException
     * @throws JobRestartException
     * @throws JobInstanceAlreadyCompleteException
     * @throws JobParametersInvalidException
     */
    private void notifyRenewalOfSubscription(LocalDate nextRenewalDate, String remainingDate)
            throws JobExecutionAlreadyRunningException, JobRestartException,
            JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("nextRenewalDate", nextRenewalDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .addString("remainingDate", remainingDate)
                .addDate("currentTime", new Date())
                .toJobParameters();

        jobLauncher.run(notifyRenewalOfSubscriptionJob, jobParameters);
    }
}
