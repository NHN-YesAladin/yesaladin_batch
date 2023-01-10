package shop.yesaladin.batch.batchstep;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import shop.yesaladin.batch.dto.MemberPaymentDto;

/**
 * 매월 1일 전체 회원을 대상으로, 지난달 주문에 대한 회원별 총 주문 금액에 따른 회원의 등급을 수정하는 Batch Step 입니다.
 *
 * @author 서민지
 * @version 1.0
 */
@RequiredArgsConstructor
@Configuration
public class MemberGradeUpdateStep {

    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    // 조회 기간에 대해 회원별 주문 금액과 결제 취소 금액의 합을 조회하는 ItemReader 입니다.
    // 이때 endDate 는 매월 1일이 됩니다.
    @Bean
    @StepScope
    public JdbcPagingItemReader<MemberPaymentDto> memberPaymentDtoItemReader(
            @Value("#{jobParameters['startDate']}") String startDate,
            @Value("#{jobParameters['endDate']}") String endDate
    ) throws Exception {
        Map<String, Object> parameterValues = new HashMap<>(2);
        parameterValues.put("start_date", startDate);
        parameterValues.put("end_date", endDate);

        return new JdbcPagingItemReaderBuilder<MemberPaymentDto>()
                .name("memberPaymentItemReader")
                .dataSource(dataSource)
                .queryProvider(pagingQueryProvider())
                .parameterValues(parameterValues)
                .pageSize(10)
                .rowMapper(new BeanPropertyRowMapper<>(MemberPaymentDto.class))
                .build();
    }

    public PagingQueryProvider pagingQueryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();

        Map<String, Order> sortKey = new HashMap<>(1);
        sortKey.put("total_amount", Order.ASCENDING);

        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause(
                "m.id as member_id, sum(p.total_amount) as total_amount, sum(pc.cancel_amount) as cancel_amount");
        factoryBean.setFromClause(
                "payments as p "
                + "join member_orders as mo on p.order_id = mo.order_id "
                + "left join payment_cancels as pc on p.id = pc.payment_id "
                + "right join members as m on mo.member_id = m.id");
        factoryBean.setWhereClause(
                "(p.approved_datetime >= :start_date and p.approved_datetime < :end_date) or mo.order_id is null");
        factoryBean.setGroupClause("m.id");
        factoryBean.setSortKeys(sortKey);

//        factoryBean.setSelectClause("m.id as member_id, v.total_amount as total_amount");
//        factoryBean.setFromClause("FROM members as m "
//                + "left join "
//                + "(select mo.member_id as mid, sum(p.total_amount) as total_amount "
//                + "from payments as p "
//                + "join orders as o on o.id = p.order_id "
//                + "join member_orders as mo on mo.order_id = o.id "
//                + "where o.order_datetime >= :start_date and o.order_datetime < :end_date "
//                + "group by mo.member_id) as v on v.mid = m.id");
//        factoryBean.setSortKey("v.total_amount");

        return factoryBean.getObject();
    }

    // ItemProcessor
    // 주문 금액에서 결제 취소 금액을 제외한 순수 주문 금액을 계산합니다.
    // 순수 주문 금액에따라 회원의 등급을 수정합니다.

    @Bean
    @StepScope
    public ItemWriter<MemberPaymentDto> itemWriter() {
        return items -> {
            for (MemberPaymentDto item : items) {
                System.out.println(">> current item = " + item);
            }
        };
    }

    // javadoc 작성
    @Bean
    @JobScope
    public Step updateMemberGradeStep() throws Exception {
        return stepBuilderFactory.get("updateMemberGradeStep")
                .<MemberPaymentDto, MemberPaymentDto>chunk(10)
                .reader(memberPaymentDtoItemReader(null, null))
                .writer(itemWriter())
                .build();
    }
}
