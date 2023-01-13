package shop.yesaladin.batch.job.step;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import shop.yesaladin.batch.dto.MemberPointDto;

/**
 * 매월 1일 지난달 주문 금액에 따라 변경된 회원 등급을 참조하여 회원 등급별 포인트를 지급하는 Batch Step 입니다.
 *
 * @author 서민지
 * @since 1.0
 */
@RequiredArgsConstructor
@Configuration
public class MemberPointUpdateStep {
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    /**
     * 회원 id, 회원 등급 id, 해당 등급별 지급 포인트를 페이지 단위로 읽어옵니다.
     *
     * @return 데이터베이스에서 회원 정보를 page size 단위로 조회하는 reader
     * @throws Exception
     */
    @Bean
    public JdbcPagingItemReader<MemberPointDto> memberPointDtoItemReader() throws Exception {
        return new JdbcPagingItemReaderBuilder<MemberPointDto>()
                .name("memberPointDtoItemReader")
                .dataSource(dataSource)
                .queryProvider(pagingQueryProvider())
                .pageSize(10)
                .rowMapper(new BeanPropertyRowMapper<>(MemberPointDto.class))
                .build();
    }

    /**
     * 회원, 회원 등급 테이블을 조인하여 회원 정보와 등급별 지급 포인트를 page size 단위로 조회하는 쿼리를 작성합니다.
     *
     * @return 지정된 데이터베이스 유형에 적합한 PagingQueryProvider 인스턴스
     * @throws Exception 데이터베이스 유형을 결정하지 못할 경우 예외
     */
    public PagingQueryProvider pagingQueryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        Map<String, Order> sortKey = new HashMap<>(1);
        sortKey.put("m.member_grade_id", Order.ASCENDING);
        factoryBean.setDataSource(dataSource);

        factoryBean.setSelectClause(
                "m.id as member_id, mg.base_given_point as member_grade_point");
        factoryBean.setFromClause("members as m "
                + "inner join member_grades as mg on m.member_grade_id = mg.id");
        factoryBean.setWhereClause("mg.id > 1");
        factoryBean.setSortKeys(sortKey);

        return factoryBean.getObject();
    }

    /**
     * 회원 등급에 맞는 지급 포인트 내역을 데이터베이스의 포인트 내역 테이블에 저장합니다.
     *
     * @return 회원별 지급 포인트 내역을 삽입하는 sql 쿼리를 담은 writer
     */
    public JdbcBatchItemWriter<MemberPointDto> insertPointHistoryItemWriter() {
        return new JdbcBatchItemWriterBuilder<MemberPointDto>()
                .dataSource(dataSource)
                .sql("INSERT INTO point_histories "
                        + "VALUES (null, :memberGradePoint, now(), :memberId, 2)")
                .beanMapped()
                .build();
    }

    /**
     * 데이터베이스에서 회원과 등급별 지급 포인트 데이터를 조회하고(by reader) 지급 포인트 내역을 데이터베이스에 삽입하는(writer) Step 입니다.
     *
     * @return 지정된 reader, writer 를 가진 updateMemberStep
     * @throws Exception
     */
    @Bean
    @JobScope
    public Step updateMemberPointStep() throws Exception {
        return stepBuilderFactory
                .get("updateMemberPointStep")
                .<MemberPointDto, MemberPointDto>chunk(10)
                .reader(memberPointDtoItemReader())
                .writer(insertPointHistoryItemWriter())
                .build();
    }


}
