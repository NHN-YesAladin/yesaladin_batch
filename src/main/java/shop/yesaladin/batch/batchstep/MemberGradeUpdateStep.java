package shop.yesaladin.batch.batchstep;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import shop.yesaladin.batch.dto.MemberDto;
import shop.yesaladin.batch.mapper.MemberDtoRowMapper;
import shop.yesaladin.batch.model.MemberGrade;

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
    public JdbcPagingItemReader<MemberDto> memberDtoItemReader(
            @Value("#{jobParameters['startDate']}") String startDate,
            @Value("#{jobParameters['endDate']}") String endDate
    ) throws Exception {
        Map<String, Object> parameterValues = new HashMap<>(2);
        parameterValues.put("start_date", startDate);
        parameterValues.put("end_date", endDate);

        return new JdbcPagingItemReaderBuilder<MemberDto>().name("memberDtoItemReader")
                .dataSource(dataSource)
                .queryProvider(pagingQueryProvider())
                .parameterValues(parameterValues)
                .pageSize(10)
                .rowMapper(new MemberDtoRowMapper())
                .build();
    }

    public PagingQueryProvider pagingQueryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        Map<String, Order> sortKey = new HashMap<>(1);
        sortKey.put("member_id", Order.ASCENDING);
        factoryBean.setDataSource(dataSource);

        factoryBean.setSelectClause(
                "select m.id as member_id, m.member_grade_id, m.point, v.total_amount, v.cancel_amount");
        factoryBean.setFromClause("members as m " + "left join "
                + "(select mo.member_id as mid, sum(p.total_amount) as total_amount, sum(pc.cancel_amount) as cancel_amount "
                + "from payments as p " + "left join payment_cancels as pc on p.id = pc.payment_id "
                + "join member_orders as mo on p.order_id = mo.order_id "
                + "where p.approved_datetime >= :start_date and p.approved_datetime < :end_date "
                + "group by mo.member_id) as v on m.id = v.mid");
        factoryBean.setSortKeys(sortKey);

        return factoryBean.getObject();
    }

    // ItemProcessor
    // 주문 금액에서 결제 취소 금액을 제외한 순수 주문 금액을 계산합니다.
    // 순수 주문 금액에따라 회원의 등급을 수정합니다.
    @Bean
    @StepScope
    public ItemProcessor<MemberDto, MemberDto> memberItemProcessor() {
        return item -> {
            Long amount = item.getTotalPaymentAmount();
            MemberGrade memberGrade = MemberGrade.PLATINUM;

            if (amount < MemberGrade.BRONZE.getBaseOrderAmount()) {
                memberGrade = MemberGrade.WHITE;
            } else if (amount < MemberGrade.SILVER.getBaseOrderAmount()) {
                memberGrade = MemberGrade.BRONZE;
            } else if (amount < MemberGrade.GOLD.getBaseOrderAmount()) {
                memberGrade = MemberGrade.SILVER;
            } else if (amount < MemberGrade.PLATINUM.getBaseOrderAmount()) {
                memberGrade = MemberGrade.GOLD;
            }

            item.updateMemberGrade(memberGrade.getId());
            item.addPoint(memberGrade.getBaseGivenPoint());

            return item;
        };
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<MemberDto> itemWriter() {
        return new JdbcBatchItemWriterBuilder<MemberDto>()
                .dataSource(dataSource)
                .sql("UPDATE members SET member_grade_id = :memberGradeId, "
                        + "point = :point WHERE id = :memberId")
                .beanMapped()
                .build();
    }

    // javadoc 작성
    @Bean
    @JobScope
    public Step updateMemberGradeStep() throws Exception {
        return stepBuilderFactory.get("updateMemberGradeStep")
                .<MemberDto, MemberDto>chunk(10)
                .reader(memberDtoItemReader(null, null))
                .processor(memberItemProcessor())
                .writer(itemWriter())
                .build();
    }
}
