package shop.yesaladin.batch.order.step;

import com.nhn.dooray.client.DoorayHook;
import com.nhn.dooray.client.DoorayHookSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import shop.yesaladin.batch.order.dto.NotifyRenewalOfSubscriptionDto;
import shop.yesaladin.batch.order.listener.NotifyRenewalOfSubscriptionItemReadListener;
import shop.yesaladin.batch.order.listener.NotifyRenewalOfSubscriptionItemWriteListener;
import shop.yesaladin.batch.order.mapper.NotifyRenewalOfSubscriptionDtoMapper;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 구독이 만료되는 1달 전 부터 구독 갱신을 위한 알림을 보내는 Batch Step 입니다.
 *
 * @author 이수정
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class RenewalOfSubscriptionNotifyStep {

    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final RestTemplate restTemplate;
    private final NotifyRenewalOfSubscriptionItemReadListener itemReadListener;
    private final NotifyRenewalOfSubscriptionItemWriteListener itemWriteListener;

    private static final String DOORAY_HOOK_URL = "https://hook.dooray.com/services/3204376758577275363/3472093162960357708/YOuBRWeZSPWxAbv8s5kAZg";
    private static final int CHUNK_SIZE = 100;

    /**
     * DB 정기 구독에서 다음 갱신일이 1달/ 1주일/ 하루 남은 정기구독, 정기구독자를 조회하고 (ItemReader),
     * 조회된 정기구독자를 대상으로 알림을 보냅니다. (ItemWriter)
     *
     * @return 지정된 ItemReader, ItemWriter 를 가진 Step
     * @author 이수정
     * @since 1.0
     */
    @Bean
    @JobScope
    public Step notifyRenewalOfSubscriptionStep() {
        return stepBuilderFactory.get("notifyRenewalOfSubscriptionStep")
                .<NotifyRenewalOfSubscriptionDto, NotifyRenewalOfSubscriptionDto>chunk(CHUNK_SIZE)
                .reader(notifyRenewalOfSubscriptionItemReader(null, null))
                .writer(notifyRenewalOfSubscriptionItemWriter(null))
                .listener(itemReadListener)
                .listener(itemWriteListener)
                .build();
    }

    /**
     * 정기 구독 중 다음 갱신일이 remainingDate 만큼 남은 정기구독 책, 정기구독자 등을 조회하여 반환합니다.
     *
     * @param queryProvider   페이징 기반 ResultSet 을 탐색하는데 필요한 모든 기능을 제공하는 PagingQueryProvider
     * @param nextRenewalDate 검색조건이 될 다음 구독 갱신일
     * @return DB 에서 주문 정보를 Paging 하여 조회하는 ItemReader
     * @author 이수정
     * @since 1.0
     */
    @Bean
    @StepScope
    public JdbcPagingItemReader<NotifyRenewalOfSubscriptionDto> notifyRenewalOfSubscriptionItemReader(
            @Qualifier("notifyRenewalOfSubscriptionFactoryBean") PagingQueryProvider queryProvider,
            @Value("#{jobParameters['nextRenewalDate']}") String nextRenewalDate
    ) {
        Map<String, Object> parameterValues = new HashMap<>(1);
        parameterValues.put("nextRenewalDate", nextRenewalDate);

        return new JdbcPagingItemReaderBuilder<NotifyRenewalOfSubscriptionDto>()
                .name("notifyRenewalOfSubscriptionItemReader")
                .dataSource(dataSource)
                .queryProvider(queryProvider)
                .parameterValues(parameterValues)
                .pageSize(CHUNK_SIZE)
                .rowMapper(new NotifyRenewalOfSubscriptionDtoMapper())
                .build();
    }

    /**
     * 정기구독 관련 테이블에서 다음 갱신일이 remainingDate 만큼 남은 정기구독 책, 정기구독자 등을 조회하는 쿼리를 작성합니다.
     *
     * @param dataSource DB 의 유형을 결정하는 DataSource
     * @return ItemReader 에서 사용할 적절한 PagingQueryProvider 구현체를 제공
     * @author 이수정
     * @since 1.0
     */
    @Bean
    public SqlPagingQueryProviderFactoryBean notifyRenewalOfSubscriptionFactoryBean(DataSource dataSource) {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();

        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("s.interval_month", Order.ASCENDING);

        factoryBean.setDataSource(dataSource);

        factoryBean.setSelectClause("SELECT p.title, m.login_id, m.name, s.next_renewal_date, s.interval_month ");
        factoryBean.setFromClause("FROM subscribes AS s " +
                "INNER JOIN member_orders AS mo ON s.order_id = mo.order_id " +
                "INNER JOIN members AS m ON mo.member_id = m.id " +
                "INNER JOIN orders AS o ON mo.order_id = o.id " +
                "INNER JOIN order_products AS op ON op.order_id = o.id " +
                "INNER JOIN products AS p ON op.product_id = p.id ");
        factoryBean.setWhereClause("WHERE s.next_renewal_date = :nextRenewalDate");
        factoryBean.setSortKeys(sortKeys);

        return factoryBean;
    }

    /**
     * 조회된 정기 구독 관련 정보를 바탕으로 정기구독자에게 알람을 보냅니다.
     *
     * @param remainingDate 남은 구독일 (1달/ 1주일/ 하루)
     * @return 정기구독 알림을 보내는 ItemWriter
     * @author 이수정
     * @since 1.0
     */
    @Bean
    @StepScope
    public ItemWriter<NotifyRenewalOfSubscriptionDto> notifyRenewalOfSubscriptionItemWriter(
            @Value("#{jobParameters['remainingDate']}") String remainingDate
    ) {
        return items -> {
            for (NotifyRenewalOfSubscriptionDto item : items) {
                String text = item.getName() + "(" + item.getLoginId() + ")님, 구독하신 상품 [" + item.getTitle() + "]의 구독갱신까지 "
                        + remainingDate + " 남았습니다. " + item.getNextRenewalDate() + "에 구독(" + item.getIntervalMonth() + "개월)이 갱신됩니다.";

                sendDoorayHook(text);
            }
        };
    }

    /**
     * DoorayHookSender를 통해 구독 갱신 알림을 보내고, 예외발생 시 보내지 못한 담아 로그를 남깁니다.
     *
     * @param text 알림메세지
     * @author 이수정
     * @since 1.0
     */
    private void sendDoorayHook(String text) {
        try {
            new DoorayHookSender(restTemplate, DOORAY_HOOK_URL)
                    .send(DoorayHook.builder()
                            .botName("구독 갱신 알림봇")
                            .text(text)
                            .build());
            Thread.sleep(1000);
        } catch (Exception e) {
            log.error("Send Failed = " + text);
        }
    }

}
