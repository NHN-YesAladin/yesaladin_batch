package shop.yesaladin.batch.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StepLoggingListener {

    private static final String START_MESSAGE = "Step %s is beginning execution.";
    private static final String END_MESSAGE = "Step %s has completed with the status %s.";

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        log.info(String.format(START_MESSAGE, stepExecution.getStepName()));
    }

    @AfterStep
    public void afterStep(StepExecution stepExecution) {
        log.info(String.format(
                END_MESSAGE,
                stepExecution.getStepName(),
                stepExecution.getStatus()
        ));
    }
}
