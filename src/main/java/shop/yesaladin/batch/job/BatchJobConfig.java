package shop.yesaladin.batch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch 설정 입니다.
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

    /**
     * 지난달 주문 금액에 따라 회원의 등급을 수정하는 step 과 등급별 포인트를 지급하는 step 을 수행하는 Job 입니다.
     *
     * @return 2개의 step 을 실행하는 updateMemberJob
     */
    @Bean
    public Job updateMemberJob() {
        return jobBuilderFactory
                .get("updateMemberJob")
                .start(updateMemberGradeStep)
                .next(updateMemberPointStep)
                .build();
    }
}
