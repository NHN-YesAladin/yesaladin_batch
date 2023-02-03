package shop.yesaladin.batch.batch.step;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import shop.yesaladin.batch.batch.dto.CouponRequestDto;
import shop.yesaladin.batch.batch.dto.CouponResponseDto;
import shop.yesaladin.batch.batch.dto.MemberCouponRequestDto;
import shop.yesaladin.batch.batch.dto.MemberDto;
import shop.yesaladin.batch.config.ServerMetaConfig;
import shop.yesaladin.common.dto.ResponseDto;
import shop.yesaladin.coupon.trigger.TriggerTypeCode;

/**
 * Shop 과 Coupon 서버의 API 통신을 통해 생일인 회원에게 생일 쿠폰을 발급하는 Batch Step 입니다.
 *
 * @author 서민지
 * @since 1.0
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class BirthdayCouponStep {

    private final StepBuilderFactory stepBuilderFactory;
    private final RestTemplate restTemplate;
    private final ServerMetaConfig serverMetaConfig;
    private List<CouponResponseDto> couponResponseDtoList;
    private int currentIndex = 0;
    private static final int CHUNK_SIZE = 500;

    /**
     * n 일 후가 생일인 회원을 조회합니다. 조회한 생일 회원이 없는 경우 null 을 반환합니다.
     *
     * @param laterDays 오늘 날짜를 기준으로 생일을 계산할 일수
     * @return 생일인 회원 목록을 조회하는 ListItemReader
     */
    @Bean
    @StepScope
    public ItemReader<MemberDto> listItemReader(@Value("#{jobParameters['laterDays']}") Integer laterDays) {
        resetCurrentIndex();
        List<MemberDto> memberIdList = getBirthdayMemberList(laterDays);
        if (memberIdList.isEmpty()) {
            log.info("=== Birthday membership list is empty. ===");
            return null;
        }
        requestBirthdayCoupon(memberIdList.size());
        return new ListItemReader<>(memberIdList);
    }

    /**
     * 회원과 쿠폰 정보를 처리하여 item 을 회원 쿠폰 등록 요청 dto 로 변환합니다.
     *
     * @return 회원 쿠폰 등록 요청 dto 로 변환하는 ItemProcessor
     */
    @Bean
    public ItemProcessor<MemberDto, MemberCouponRequestDto> itemProcessor() {
        return item -> {
            MemberCouponRequestDto dto = new MemberCouponRequestDto(item.getMemberId());
            this.couponResponseDtoList.forEach(coupon -> {
                dto.getCouponCodes().add(coupon.getCreatedCouponCodes().get(currentIndex++));
                dto.getCouponGroupCodes().add(coupon.getCouponGroupCode());
            });
            return dto;
        };
    }

    /**
     * 회원 쿠폰 등록을 요청합니다.
     *
     * @return 회원 쿠폰 등록을 요청하는 ItemWriter
     */
    @Bean
    public ItemWriter<MemberCouponRequestDto> itemWriter() {
        return this::registerMemberCoupon;
    }

    /**
     * Coupon 서버에 생일 쿠폰 코드를 요청하여 Step 의 couponResponseDtoList 필드에 저장합니다.
     *
     * @param quantity 요청하는 수량
     */
    private void requestBirthdayCoupon(int quantity) {
        CouponRequestDto couponRequestDto = new CouponRequestDto(TriggerTypeCode.BIRTHDAY,
                quantity
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CouponRequestDto> request = new HttpEntity<>(
                couponRequestDto,
                headers
        );

        ResponseEntity<ResponseDto<List<CouponResponseDto>>> response = restTemplate.exchange(
                serverMetaConfig.getCouponServerUrl() + "/v1/issuances",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {}
        );

        this.couponResponseDtoList = Objects.requireNonNull(response.getBody()).getData();
    }

    /**
     * Shop, Coupon 서버와의 API 통신으로 생일인 회원에게 쿠폰을 지급하는 Step 입니다.
     *
     * @return giveBirthdayCouponStep
     */
    @Bean
    public Step giveBirthdayCouponStep() {
        return stepBuilderFactory.get("giveBirthdayCouponStep")
                .<MemberDto, MemberCouponRequestDto>chunk(CHUNK_SIZE)
                .reader(listItemReader(null))
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    /**
     * 생일 회원을 조회합니다.
     *
     * @param laterDays 오늘 날짜를 기준으로 생일을 계산할 일수
     * @return laterDays 후가 생일인 회원 목록
     */
    private List<MemberDto> getBirthdayMemberList(int laterDays) {
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(
                        serverMetaConfig.getShopServerUrl() + "/v1/members")
                .queryParam("type=birthday", (Object) null)
                .queryParam("laterDays", laterDays)
                .build();

        ResponseEntity<ResponseDto<List<MemberDto>>> responseEntity = restTemplate.exchange(uriComponents.toUri(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        return Objects.requireNonNull(responseEntity.getBody()).getData();
    }

    /**
     * Shop 서버에게 회원 쿠폰 등록을 요청합니다.
     *
     * @param items 회원 쿠폰 등록 요청 리스트
     */
    private void registerMemberCoupon(List<? extends MemberCouponRequestDto> items) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<? extends List<? extends MemberCouponRequestDto>> request = new HttpEntity<>(
                items,
                headers
        );

        restTemplate.exchange(serverMetaConfig.getShopServerUrl() + "/v1/coupons",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {}
        );
    }

    /**
     * currentIndex 를 초기화합니다.
     */
    private void resetCurrentIndex() {
        this.currentIndex = 0;
    }
}
