package shop.yesaladin.batch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import shop.yesaladin.batch.job.incrementer.DailyJobTimestamper;

/**
 * Spring Batch 설정 파일입니다.
 *
 * @author 서민지
 * @since 1.0
 */
@RequiredArgsConstructor
@EnableBatchProcessing
@Configuration
public class BatchJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final Step updateMemberGradeStep;
    private final Step updateMemberPointStep;

    @Bean
    public Job job() {
        return jobBuilderFactory
                .get("updateMemberJob")
                .start(updateMemberGradeStep)
                .next(updateMemberPointStep)
                .incrementer(new DailyJobTimestamper())
                .build();
    }
}
