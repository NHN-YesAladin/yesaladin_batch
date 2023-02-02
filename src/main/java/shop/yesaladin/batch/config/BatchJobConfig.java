package shop.yesaladin.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
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
    private final Step giveBirthdayCouponStep;

    /**
     * updateMemberJob 의 필수 파라미터를 지정하는 validator 입니다.
     *
     * @return 조회 기간 설정에 필요한 필수 파라미터를 가진 validator
     */
    @Bean
    public JobParametersValidator validator() {
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();

        validator.setRequiredKeys(new String[] {"startDate", "endDate"});

        return validator;
    }

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
                .validator(validator())
                .build();
    }

    @Bean
    public Job giveBirthdayCouponJob() {
        return jobBuilderFactory
                .get("giveBirthdayCouponJob")
                .start(giveBirthdayCouponStep)
                .build();
    }
}
