package shop.yesaladin.batch.order.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import shop.yesaladin.batch.batch.listener.JobLoggingListener;

/**
 * Spring Batch Job 설정 입니다.
 *
 * @author 이수정
 * @since 1.0
 */
@RequiredArgsConstructor
@EnableBatchProcessing
@Configuration
public class OrderJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final Step insertOrderStatusChangeLogStep;
    private final Step notifyRenewalOfSubscriptionStep;

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
                .listener(jobLoggingListener)
                .build();
    }

    /**
     * 구독이 만료되는 1달 전 부터 구독 갱신을 위한 알림을 보내는 Step 을 수행하는 Job 입니다.
     *
     * @return notifyRenewalOfSubscriptionStep 을 실행하는 Job
     * @author 이수정
     * @since 1.0
     */
    @Bean
    public Job notifyRenewalOfSubscriptionJob() {
        return jobBuilderFactory
                .get("notifyRenewalOfSubscriptionJob")
                .start(notifyRenewalOfSubscriptionStep)
                .listener(jobLoggingListener)
                .build();
    }

    /**
     * 구독이 만료되는 1달 전 부터 구독 갱신을 위한 알림을 보내는 Step 을 수행하는 Job 입니다.
     *
     * @return notifyRenewalOfSubscriptionStep 을 실행하는 Job
     * @author 이수정
     * @since 1.0
     */
    @Bean
    public Job notifyRenewalOfSubscriptionJob() {
        return jobBuilderFactory
                .get("notifyRenewalOfSubscriptionJob")
                .start(notifyRenewalOfSubscriptionStep)
                .build();
    }
}
