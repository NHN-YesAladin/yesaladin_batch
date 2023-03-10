package shop.yesaladin.batch.order.step;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import shop.yesaladin.batch.order.dto.OrderStatusChangeLogDto;
import shop.yesaladin.batch.order.listener.OrderStatusChangeLogListener;
import shop.yesaladin.batch.order.mapper.OrderStatusChangeLogDtoRowMapper;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 주문 상태 변경 이력을 조회하여 주문(ORDER) 상태로 3일 지난 주문를 취소(CANCEL) 상태로 추가 기록하는 Batch Step 입니다.
 *
 * @author 이수정
 * @since 1.0
 */
@RequiredArgsConstructor
@Configuration
public class OrderStatusChangeLogInsertStep {

    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final OrderStatusChangeLogListener listener;

    private static final int CHUNK_SIZE = 100;

    /**
     * DB 주문 상태 변경 이력에서 상태가 주문(ORDER)이고 3일 이상 지난 주문을 조회하고 (ItemReader),
     * 조회된 주문을 대상으로 취소(CANCEL) 상태 변경 이력을 추가 삽입합니다. (ItemWriter)
     *
     * @return 지정된 ItemReader, ItemWriter 를 가진 Step
     * @author 이수정
     * @since 1.0
     */
    @Bean
    @JobScope
    public Step insertOrderStatusChangeLogStep() {
        return stepBuilderFactory.get("insertOrderStatusChangeLogStep")
                .<OrderStatusChangeLogDto, OrderStatusChangeLogDto>chunk(CHUNK_SIZE)
                .reader(orderStatusChangeLogItemReader(null, null))
                .writer(orderStatusChangeLogItemWriter(null))
                .listener(listener)
                .faultTolerant()
                .retry(Exception.class)
                .noRetry(SQLException.class)
                .retryLimit(3)
                .build();
    }

    /**
     * 주문 상태 변경 이력에서 상태가 주문(ORDER)이고 3일 이상 지난 주문을 조회하여 반환합니다.
     *
     * @param queryProvider    페이징 기반 ResultSet 을 탐색하는데 필요한 모든 기능을 제공하는 PagingQueryProvider
     * @param threeDaysAgoDate 기준이 되는 3일 전의 날짜
     * @return DB 에서 주문 정보를 Paging 하여 조회하는 ItemReader
     * @author 이수정
     * @since 1.0
     */
    @Bean
    @StepScope
    public JdbcPagingItemReader<OrderStatusChangeLogDto> orderStatusChangeLogItemReader(
            @Qualifier("orderStatusChangeLogFactoryBean") PagingQueryProvider queryProvider,
            @Value("#{jobParameters['threeDaysAgoDate']}") String threeDaysAgoDate
    ) {
        Map<String, Object> parameterValues = new HashMap<>(1);
        parameterValues.put("threeDaysAgoDate", threeDaysAgoDate);

        return new JdbcPagingItemReaderBuilder<OrderStatusChangeLogDto>()
                .name("orderStatusChangeLogItemReader")
                .dataSource(dataSource)
                .queryProvider(queryProvider)
                .parameterValues(parameterValues)
                .pageSize(CHUNK_SIZE)
                .rowMapper(new OrderStatusChangeLogDtoRowMapper())
                .build();
    }

    /**
     * 주문 상태 변경 이력 테이블에서 상태가 주문(ORDER)이고 3일 이상 지난 주문을 조회하는 쿼리를 작성합니다.
     *
     * @param dataSource DB 의 유형을 결정하는 DataSource
     * @return ItemReader 에서 사용할 적절한 PagingQueryProvider 구현체를 제공
     * @author 이수정
     * @since 1.0
     */
    @Bean
    public SqlPagingQueryProviderFactoryBean orderStatusChangeLogFactoryBean(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();

        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("l.change_datetime", Order.ASCENDING);

        factoryBean.setDataSource(dataSource);

        factoryBean.setSelectClause("SELECT l.order_id, l.change_datetime, l.order_status_code_id ");
        factoryBean.setFromClause("FROM order_status_change_logs AS l " +
                "LEFT JOIN (" +
                "SELECT o.order_id " +
                "FROM order_status_change_logs AS o " +
                "WHERE o.order_status_code_id != (select id from order_status_codes where status = 'ORDER') " +
                "GROUP BY o.order_id) AS o ON l.order_id = o.order_id ");
        factoryBean.setWhereClause("WHERE DATE(l.change_datetime) <= :threeDaysAgoDate AND o.order_id is null");
        factoryBean.setSortKeys(sortKeys);

        return factoryBean;
    }

    /**
     * 조회된 주문을 대상으로 취소(CANCEL) 상태 변경 이력을 삽입합니다.
     *
     * @param dataSource DB 의 유형을 결정하는 DataSource
     * @return 주문 상태 변경 이력을 삽입하는 쿼리를 담은 ItemWriter
     * @author 이수정
     * @since 1.0
     */
    @Bean
    @StepScope
    public JdbcBatchItemWriter<OrderStatusChangeLogDto> orderStatusChangeLogItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<OrderStatusChangeLogDto>()
                .dataSource(dataSource)
                .sql("INSERT INTO order_status_change_logs (change_datetime, order_id, order_status_code_id) " +
                        "VALUES (now(), :orderId, (select id from order_status_codes where status = 'CANCEL'))")
                .beanMapped()
                .build();
    }
}
