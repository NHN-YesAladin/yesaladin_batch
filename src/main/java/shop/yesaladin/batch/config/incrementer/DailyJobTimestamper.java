package shop.yesaladin.batch.config.incrementer;

import java.util.Date;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;

/**
 * 동일한 잡을 여러 번 수행할 수 있도록 Date 를 사용하여 잡에게 파라미터로 전달하는 JobParametersIncrementer 입니다.
 *
 * @author 서민지
 * @since 1.0
 */
public class DailyJobTimestamper implements JobParametersIncrementer {

    @Override
    public JobParameters getNext(JobParameters parameters) {
        return new JobParametersBuilder(parameters).addDate("currentDate", new Date())
                .toJobParameters();
    }
}