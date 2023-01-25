package shop.yesaladin.batch.job.step;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import shop.yesaladin.batch.dto.CouponRequestDto;
import shop.yesaladin.batch.dto.CouponResponseDto;
import shop.yesaladin.batch.dto.MemberCouponDto;
import shop.yesaladin.batch.repository.QueryMemberRepository;
import shop.yesaladin.coupon.trigger.TriggerTypeCode;

@RequiredArgsConstructor
@Configuration
public class BirthdayCouponStep {

    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final QueryMemberRepository queryMemberRepository;
    private final RestTemplate restTemplate;
    private final int chunkSize = 10;

    private LocalDate getBirthday() {
        return LocalDate.now().plusDays(7);
    }

    @Bean
    public ItemReader<MemberCouponDto> listItemReader() {
        // 생일인 회원을 읽는다.
        List<MemberCouponDto> list = queryMemberRepository.findMemberIdsByBirthday(
                getBirthday().getMonth().getValue(),
                getBirthday().getDayOfMonth()
        );

        return new ListItemReader<>(list);
    }

    @Bean
    public ItemProcessor<MemberCouponDto, MemberCouponDto> listItemProcessor() {
        // 쿠폰 서버에서 생일쿠폰 코드 가져오기, 요청하는 쿠폰의 개수는 chunkSize 와 같다.

        List<String> couponCodes = getCouponCodes();
        AtomicInteger index = new AtomicInteger();
        return item -> {
            item.setCouponCode(couponCodes.get(index.getAndIncrement()));
            return item;
        };
    }

    @Bean
    public JdbcBatchItemWriter<MemberCouponDto> jdbcBatchItemWriter() {
        // Shop DB 의 회원 쿠폰 테이블에 레코드를 삽입한다.
        return new JdbcBatchItemWriterBuilder<MemberCouponDto>().dataSource(dataSource)
                .sql("INSERT INTO member_coupons "
                        + "VALUES (null, :memberId, :couponCode)")
                .beanMapped()
                .build();
    }

    private List<String> getCouponCodes() {
        CouponRequestDto couponRequestDto = new CouponRequestDto(
                TriggerTypeCode.BIRTHDAY,
                this.chunkSize
        );

        ResponseEntity<CouponResponseDto> responseEntity = restTemplate.postForEntity(
                requestUrl(),
                couponRequestDto,
                CouponResponseDto.class
        );

        return Objects.requireNonNull(responseEntity.getBody()).getCouponCodes();
    }

    private String requestUrl() {
        return "http://localhost:8085/v1/issuances";
    }

    @Bean
    public Step giveBirthdayCouponStep() {
        return stepBuilderFactory.get("giveBirthdayCouponStep")
                .<MemberCouponDto, MemberCouponDto>chunk(chunkSize)
                .reader(listItemReader())
                .processor(listItemProcessor())
                .writer(jdbcBatchItemWriter())
                .build();
    }
}
