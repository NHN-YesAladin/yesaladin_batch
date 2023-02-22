package shop.yesaladin.batch.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.stereotype.Component;

/**
 * Job 실행 시작과 끝에 로그를 작성하는 리스너입니다.
 *
 * @author 서민지
 * @since 1.0
 */
@Slf4j
@Component
public class JobLoggingListener {

    private static final String START_MESSAGE = "Job %s is beginning execution.";
    private static final String END_MESSAGE = "Job %s has completed with the status %s.";

    /**
     * 잡 실행 시작시 로그를 작성합니다.
     *
     * @param jobExecution 실행되는 잡의 execution
     */
    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        log.info(String.format(START_MESSAGE, jobExecution.getJobInstance().getJobName()));
    }

    /**
     * 잡 실행 종료시 로그를 작성합니다.
     *
     * @param jobExecution 실행되는 잡의 execution
     */
    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        log.info(String.format(
                END_MESSAGE,
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus()
        ));
    }
}
