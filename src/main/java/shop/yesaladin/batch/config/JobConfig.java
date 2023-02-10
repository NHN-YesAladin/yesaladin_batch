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
 * @author 이수정
 * @since 1.0
 */
@RequiredArgsConstructor
@EnableBatchProcessing
@Configuration
public class JobConfig {

    private final JobBuilderFactory jobBuilderFactory;

    private final Step updateMemberGradeStep;
    private final Step updateMemberPointStep;
    private final Step giveBirthdayCouponStep;

    private final Step insertOrderStatusChangeLogStep;

    /**
     * updateMemberJob 의 필수 파라미터를 지정하는 validator 입니다.
     *
     * @return 조회 기간 설정에 필요한 필수 파라미터를 가진 validator
     */
    @Bean
    public JobParametersValidator validator() {
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();

        validator.setRequiredKeys(new String[]{"startDate", "endDate"});

        return validator;
    }

    /**
     * 지난달 주문 금액에 따라 회원의 등급을 수정하는 step 과 등급별 포인트를 지급하는 step 을 수행하는 Job 입니다.
     *
     * @return updateMemberGradeStep, updateMemberPointStep 을 실행하는 Job
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

    /**
     * 생일 회원을 조회하여 쿠폰을 지급하는 Step 을 수행하는 Job 입니다.
     *
     * @return giveBirthdayCouponStep 을 실행하는 Job
     */
    @Bean
    public Job giveBirthdayCouponJob() {
        return jobBuilderFactory
                .get("giveBirthdayCouponJob")
                .start(giveBirthdayCouponStep)
                .build();
    }

    /**
     * 주문 상태 변경 이력 테이블에 기록된 가장 최근 상태가 주문(ORDER)인 채로 3일이 지난 주문을 대상으로,
     * 주문 상태 변경 이력 테이블에 취소(CANCEL) 상태를 추가 기록해주는 Step 을 수행하는 Job 입니다.
     *
     * @return insertOrderStatusChangeLogStep 을 실행하는 Job
     * @author 이수정
     * @since 1.0
     */
    @Bean
    public Job insertOrderStatusChangeLogJob() {
        return jobBuilderFactory
                .get("insertOrderStatusChangeLogJob")
                .start(insertOrderStatusChangeLogStep)
                .build();
    }
}
