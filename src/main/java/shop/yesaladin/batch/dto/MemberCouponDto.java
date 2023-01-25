package shop.yesaladin.batch.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
public class MemberCouponDto {

    private String id;
    @Setter
    private String couponCode;
}
