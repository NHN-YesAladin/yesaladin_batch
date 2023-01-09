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
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import shop.yesaladin.batch.dto.MemberPaymentDto;

/**
 * 매월 1일 전 달에 주문을 한 회원을 대상으로, 총 주문금액에 따른 회원의 등급을 수정하는 Batch Step 입니다.
 *
 * @author 서민지
 * @version 1.0
 */
@RequiredArgsConstructor
@Configuration
public class MemberGradeUpdateStep {

    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    // javadoc 작성
    @Bean
    @JobScope
    public Step updateMemberGradeStep() throws Exception {
        return stepBuilderFactory.get("updateMemberGradeStep")
                .<MemberPaymentDto, MemberPaymentDto>chunk(10)
                .reader(memberPaymentItemReader(null, null))
                .writer(itemWriter())
                .build();
    }

    // 조회 기간에 대해 주문이 존재하는 회원과 회원별 총 주문 금액을 조회하는 ItemReader 입니다.
    // 이때 endDate 는 매월 1일이 됩니다.
    @Bean
    @StepScope
    public JdbcPagingItemReader<MemberPaymentDto> memberPaymentItemReader(
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

        factoryBean.setSelectClause("select mo.member_id as member_id, sum(p.total_amount) as total_amount");
        factoryBean.setFromClause("from member_orders as mo "
                + "inner join orders as o on mo.order_id = o.id "
                + "inner join payments as p on o.id = p.order_id ");
        factoryBean.setWhereClause("where o.order_datetime >= :start_date and o.order_datetime < :end_date");
        factoryBean.setGroupClause("group by mo.member_id");
        factoryBean.setSortKey("mo.member_id");
        factoryBean.setDataSource(dataSource);

        return factoryBean.getObject();
    }

    @Bean
    @StepScope
    public ItemWriter<MemberPaymentDto> itemWriter() {
        return items -> {
            for (MemberPaymentDto item : items) {
                System.out.println(">> current item = " + item);
            }
        };
    }


}
