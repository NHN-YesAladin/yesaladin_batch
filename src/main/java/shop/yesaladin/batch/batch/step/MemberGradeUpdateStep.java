package shop.yesaladin.batch.batch.step;

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
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import shop.yesaladin.batch.batch.dto.MemberGradeDto;
import shop.yesaladin.batch.batch.mapper.MemberGradeDtoRowMapper;
import shop.yesaladin.batch.batch.model.MemberGrade;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 매월 1일 전체 회원을 대상으로 지난달 주문에 대한 회원별 주문 금액을 산정하여 회원의 등급을 수정하는 Batch Step 입니다.
 *
 * @author 서민지
 * @since 1.0
 */
@RequiredArgsConstructor
@Configuration
public class MemberGradeUpdateStep {

    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private static final int CHUNK_SIZE = 100;

    /**
     * 회원 정보와 조회 기간에 대한 주문 및 결제 취소 금액을 페이지 단위로 읽어옵니다.
     *
     * @param startDate 주문 조회 기간의 시작 날짜, 지난달 1일
     * @param endDate   주문 조회 기간 마지막 날짜의 다음날, 이번달 1일
     * @return 데이터베이스에서 회원 정보를 page size 단위로 조회하는 reader
     * @throws Exception
     */
    @Bean
    @StepScope
    public JdbcPagingItemReader<MemberGradeDto> memberGradeDtoItemReader(
            @Value("#{jobParameters['startDate']}") String startDate,
            @Value("#{jobParameters['endDate']}") String endDate
    ) throws Exception {
        Map<String, Object> parameterValues = new HashMap<>(2);
        parameterValues.put("start_date", startDate);
        parameterValues.put("end_date", endDate);

        return new JdbcPagingItemReaderBuilder<MemberGradeDto>()
                .name("memberGradeDtoItemReader")
                .dataSource(dataSource)
                .queryProvider(pagingQueryProvider())
                .parameterValues(parameterValues)
                .pageSize(CHUNK_SIZE)
                .rowMapper(new MemberGradeDtoRowMapper())
                .build();
    }

    /**
     * 회원, 주문, 결제 관련 테이블을 조인하여 회원 정보와 지정된 조회 기간에 대한 주문 및 결제 취소 금액을 page size 단위로 조회하는 쿼리를 작성합니다.
     *
     * @return 지정된 데이터베이스 유형에 적합한 PagingQueryProvider 인스턴스
     * @throws Exception 데이터베이스 유형을 결정하지 못할 경우 예외
     */
    public PagingQueryProvider pagingQueryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        Map<String, Order> sortKey = new HashMap<>(1);
        sortKey.put("member_id", Order.ASCENDING);
        factoryBean.setDataSource(dataSource);

        factoryBean.setSelectClause(
                "m.id as member_id, v.total_amount - v.cancel_amount as pay_amount");
        factoryBean.setFromClause("members as m left join "
                + "(select mo.member_id as mid, sum(p.total_amount) as total_amount, sum(pc.cancel_amount) as cancel_amount "
                + "from payments as p left join payment_cancels as pc on p.id = pc.payment_id "
                + "join member_orders as mo on p.order_id = mo.order_id "
                + "where p.approved_datetime >= :start_date and p.approved_datetime < :end_date "
                + "group by mo.member_id) as v on m.id = v.mid");
        factoryBean.setSortKeys(sortKey);

        return factoryBean.getObject();
    }

    /**
     * 지난달 순수 주문 금액에 따라 MemberGradeDto 의 회원 등급을 수정하는 비즈니스 로직을 수행합니다.
     *
     * @return 회원 정보를 업데이트하는 비즈니스 로직을 수행하는 processor
     */
    @Bean
    public ItemProcessor<MemberGradeDto, MemberGradeDto> memberGradeDtoItemProcessor() {
        return item -> {
            item.updateMemberGrade(getMemberGradeId(item.getPayAmount()));

            return item;
        };
    }

    public int getMemberGradeId(Long payAmount) {
        MemberGrade memberGrade = MemberGrade.PLATINUM;

        if (payAmount < MemberGrade.BRONZE.getBaseOrderAmount()) {
            memberGrade = MemberGrade.WHITE;
        } else if (payAmount < MemberGrade.SILVER.getBaseOrderAmount()) {
            memberGrade = MemberGrade.BRONZE;
        } else if (payAmount < MemberGrade.GOLD.getBaseOrderAmount()) {
            memberGrade = MemberGrade.SILVER;
        } else if (payAmount < MemberGrade.PLATINUM.getBaseOrderAmount()) {
            memberGrade = MemberGrade.GOLD;
        }

        return memberGrade.getId();
    }

    /**
     * 수정된 회원 등급을 데이터베이스의 회원 테이블에 업데이트합니다.
     *
     * @return 변경된 회원 데이터가 매핑된 sql 쿼리를 담은 writer
     */
    @Bean
    public JdbcBatchItemWriter<MemberGradeDto> updateMemberItemWriter() {
        return new JdbcBatchItemWriterBuilder<MemberGradeDto>().dataSource(dataSource)
                .sql("UPDATE members SET member_grade_id = :memberGradeId WHERE id = :memberId")
                .beanMapped()
                .build();
    }

    /**
     * 회원의 등급 변경 내역을 데이터베이스의 등급 변경 내역 테이블에 저장합니다.
     *
     * @return 회원 등급 변경 내역을 삽입하는 sql 쿼리를 담은 writer
     */
    @Bean
    public JdbcBatchItemWriter<MemberGradeDto> insertMemberGradeHistoryItemWriter() {
        return new JdbcBatchItemWriterBuilder<MemberGradeDto>().dataSource(dataSource)
                .sql("INSERT INTO member_grade_histories "
                        + "VALUES (null, now(), :payAmount, :memberGradeId, :memberId)")
                .beanMapped()
                .build();
    }

    /**
     * Step 의 출력 부분을 구성하는 2개의 writer 를 래핑하는 CompositeItemWriter 입니다.
     *
     * @return updateMemberItemWriter, insertMemberGradeHistoryItemWriter 에게 출력을 위임하는 래핑 writer
     */
    @Bean
    public CompositeItemWriter<MemberGradeDto> compositeItemWriter() {
        return new CompositeItemWriterBuilder<MemberGradeDto>()
                .delegates(Arrays.asList(
                        updateMemberItemWriter(),
                        insertMemberGradeHistoryItemWriter()
                ))
                .build();
    }

    /**
     * 데이터베이스에서 회원과 주문 데이터를 조회하고(by reader) 주문 금액에 따라 회원 데이터를 수정하여(by processor) 이를 데이터베이스에
     * 업데이트하는(writer) Step 입니다.
     *
     * @return 지정된 reader, processor, writer 를 가진 updateMemberStep
     * @throws Exception
     */
    @Bean
    @JobScope
    public Step updateMemberGradeStep() throws Exception {
        return stepBuilderFactory.get("updateMemberGradeStep")
                .<MemberGradeDto, MemberGradeDto>chunk(CHUNK_SIZE)
                .reader(memberGradeDtoItemReader(null, null))
                .processor(memberGradeDtoItemProcessor())
                .writer(compositeItemWriter())
                .build();
    }
}
