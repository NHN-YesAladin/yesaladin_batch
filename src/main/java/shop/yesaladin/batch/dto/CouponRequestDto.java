package shop.yesaladin.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import shop.yesaladin.coupon.trigger.TriggerTypeCode;

@Getter
@AllArgsConstructor
public class CouponRequestDto {

    private TriggerTypeCode triggerTypeCode;
    private int quantity;
}
