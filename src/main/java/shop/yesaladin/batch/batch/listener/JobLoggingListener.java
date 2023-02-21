package shop.yesaladin.batch.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobLoggingListener {

    private static final String START_MESSAGE = "Job %s is beginning execution.";
    private static final String END_MESSAGE = "Job %s has completed with the status %s.";

    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        log.info(String.format(START_MESSAGE, jobExecution.getJobInstance().getJobName()));
    }

    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        log.info(String.format(
                END_MESSAGE,
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus()
        ));
    }
}
