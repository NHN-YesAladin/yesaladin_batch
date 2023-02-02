package shop.yesaladin.batch.batch.step;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import shop.yesaladin.batch.batch.dto.CouponRequestDto;
import shop.yesaladin.batch.batch.dto.CouponResponseDto;
import shop.yesaladin.batch.batch.dto.MemberCouponRequestDto;
import shop.yesaladin.batch.batch.dto.MemberDto;
import shop.yesaladin.batch.batch.model.Member;
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
    private final int CHUNK_SIZE = 500;
    private List<CouponResponseDto> couponResponseDtoList;
    private int currentIndex = 0;
    private final int LATER_DAYS = 7;

    @Bean
    @StepScope
    public ItemReader<Member> listItemReader() {
        // TODO : LATER_DAYS jobParameters 로 빼서 사용하기
        resetCurrentIndex();
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(
                        serverMetaConfig.getShopServerUrl() + "/v1/members")
                .queryParam("type=birthday", (Object) null)
                .queryParam("laterDays", LATER_DAYS)
                .build();

        log.info("생일자 조회 요청 url {}", uriComponents.toUri());

        ResponseEntity<List<Member>> responseEntity = restTemplate.exchange(
                uriComponents.toUri(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        List<Member> memberList = responseEntity.getBody();
        log.info("생일자 List<Member> 사이즈 {}", memberList.size());

        // Coupon API 를 호출하여 생일 회원 수만큼 생일쿠폰을 발행합니다.
        // 전역변수 couponResponseDtoList 에 저장합니다.
        this.couponResponseDtoList = getBirthdayCouponResponseDtoList(memberList.size());

        return new ListItemReader<>(memberList);
    }

    @Bean
    @StepScope
    public ItemProcessor<Member, MemberCouponRequestDto> itemProcessor() {
        return item -> {
            // currentIndex 를 증가시키며 MemberCouponRequestDto 를 생성합니다.
            MemberCouponRequestDto dto = new MemberCouponRequestDto(MemberDto.fromEntity(item));
            log.info("processor** 멤버쿠폰요청 {}", dto);
            for (CouponResponseDto coupon : this.couponResponseDtoList) {
                dto.getCouponCodes().add(coupon.getCreatedCouponCodes().get(currentIndex++));
//                dto.getGroupCodes().add(coupon.getGroupCodes().get(currentIndex));
                dto.getGroupCodes().add("9030ff47-2456-4bb8-a604-cf37f8daef31");
            }
            log.info(
                    "멤버쿠폰 요청 dto {} {}",
                    dto.getMemberDto().getName(),
                    dto.getCouponCodes().get(0)
            );
            return dto;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<MemberCouponRequestDto> itemWriter() {
        log.info("writer**");
        return items -> {
            log.info("inner writer.....");
            RequestEntity<? extends List<? extends MemberCouponRequestDto>> request = RequestEntity.post(
                            serverMetaConfig.getShopServerUrl() + "/v1/coupons")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(items);

            restTemplate.exchange(
                    serverMetaConfig.getShopServerUrl() + "/v1/coupons",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {
                    }
            );
        };
    }

    /**
     * 생일자 회원 수만큼 Coupon 서버에 생일 쿠폰 코드를 요청합니다.
     *
     * @param quantity
     * @return
     */
    private List<CouponResponseDto> getBirthdayCouponResponseDtoList(int quantity) {
        CouponRequestDto couponRequestDto = new CouponRequestDto(
                TriggerTypeCode.BIRTHDAY,
                quantity
        );

        RequestEntity<CouponRequestDto> request = RequestEntity.post(
                        serverMetaConfig.getCouponServerUrl() + "/v1/issuances")
                .contentType(MediaType.APPLICATION_JSON)
                .body(couponRequestDto);

        log.info("생일쿠폰 코드 발급 요청 url {}", serverMetaConfig.getCouponServerUrl() + "/v1/issuances");
        ResponseEntity<ResponseDto<List<CouponResponseDto>>> response = restTemplate.exchange(
                serverMetaConfig.getCouponServerUrl() + "/v1/issuances",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {
                }
        );

        List<CouponResponseDto> data = response.getBody().getData();
        log.info("발행된 쿠폰 종류 {}", data.size());
        log.info("발행된 쿠폰0 의 수량 {}", data.get(0).getCreatedCouponCodes().size());
        return data;
    }

    @Bean
    public Step giveBirthdayCouponStep() {
        return stepBuilderFactory.get("giveBirthdayCouponStep")
                .<Member, MemberCouponRequestDto>chunk(CHUNK_SIZE)
                .reader(listItemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    private void resetCurrentIndex() {
        this.currentIndex = 0;
    }
}
