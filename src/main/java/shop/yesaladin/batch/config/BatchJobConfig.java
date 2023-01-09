package shop.yesaladin.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import shop.yesaladin.batch.config.incrementer.DailyJobTimestamper;

/**
 * Spring Batch 설정 파일입니다.
 *
 * @author 서민지
 * @version 1.0
 */
@RequiredArgsConstructor
@EnableBatchProcessing
@Configuration
public class BatchJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final Step resetAllMemberGradeStep;
    private final Step updateMemberGradeStep;

    @Bean
    public Job job() {
        return jobBuilderFactory
                .get("manageMemberGradeJob")
//                .start(resetAllMemberGradeStep)
                .start(updateMemberGradeStep)
                .incrementer(new DailyJobTimestamper())
                .build();
    }
}
