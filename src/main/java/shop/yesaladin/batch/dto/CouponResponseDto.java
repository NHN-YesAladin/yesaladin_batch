package shop.yesaladin.batch.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CouponResponseDto {

    List<String> couponCodes;
}
