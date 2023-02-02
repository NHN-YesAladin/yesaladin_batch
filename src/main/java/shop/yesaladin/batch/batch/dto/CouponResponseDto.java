package shop.yesaladin.batch.batch.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponseDto {

    private List<String> createdCouponCodes;
    private String couponGroupCode;
}
