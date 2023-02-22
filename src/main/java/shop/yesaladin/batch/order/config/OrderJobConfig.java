package shop.yesaladin.batch.order.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch Order Job 설정 입니다.
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
