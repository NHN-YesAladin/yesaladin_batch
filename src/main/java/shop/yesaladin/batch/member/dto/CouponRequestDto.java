package shop.yesaladin.batch.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import shop.yesaladin.coupon.trigger.TriggerTypeCode;

/**
 * Coupon 서버에 쿠폰 발행 요청을 위한 정보를 담은 dto 입니다.
 *
 * @author 서민지
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public class CouponRequestDto {

    private TriggerTypeCode triggerTypeCode;
    private int quantity;
}
