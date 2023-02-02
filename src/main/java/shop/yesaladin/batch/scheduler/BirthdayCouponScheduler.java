package shop.yesaladin.batch.scheduler;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 회원 등급을 관리하는 Job 의 스케줄러 입니다.
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
    private static final String TWO_AM_1ST_OF_EVERY_MONTH = "0 * * * * *";

    /**
     * 매월 1일 02시에 조회 시작일(지난달 1일), 조회 마지막일(이번달 1일) 파라미터를 갖는 updateMemberGradeJob 을 실행합니다.
     */
    @Scheduled(cron = TWO_AM_1ST_OF_EVERY_MONTH, zone = "Asia/Seoul")
    public void scheduleUpdateMemberGrade() {
        log.info("=== giveBirthdayCoupon schedule started at {} ===", LocalDateTime.now());

        try {
            jobLauncher.run(giveBirthdayCouponJob, new JobParametersBuilder().addString("timestamp", LocalDateTime.now().toString()).toJobParameters());
        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException | JobRestartException e) {
            log.error(e.getMessage());
        }

        log.info("=== giveBirthdayCoupon schedule ended at {} ===", LocalDateTime.now());
    }
}
