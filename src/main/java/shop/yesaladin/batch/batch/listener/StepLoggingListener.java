package shop.yesaladin.batch.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.stereotype.Component;

/**
 * Step 실행 시작과 끝에 로그를 작성하는 리스너입니다.
 *
 * @author 서민지
 * @since 1.0
 */
@Slf4j
@Component
public class StepLoggingListener {

    private static final String START_MESSAGE = "Step %s is beginning execution.";
    private static final String END_MESSAGE = "Step %s has completed with the status %s.";

    /**
     * 스텝 실행 시작시 로그를 작성합니다.
     *
     * @param stepExecution 실행되는 스텝 execution
     */
    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        log.info(String.format(START_MESSAGE, stepExecution.getStepName()));
    }

    /**
     * 스텝 실행 종료시 로그를 작성합니다.
     *
     * @param stepExecution 실행되는 스텝 execution
     */
    @AfterStep
    public void afterStep(StepExecution stepExecution) {
        log.info(String.format(
                END_MESSAGE,
                stepExecution.getStepName(),
                stepExecution.getStatus()
        ));
    }
}
