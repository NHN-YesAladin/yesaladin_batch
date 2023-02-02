package shop.yesaladin.batch.batch.dto;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 요청한 쿠폰에 대한 정보를 담은 dto 입니다.
 *
 * @author 서민지
 * @since 1.0
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CouponResponseDto {

    @NotEmpty
    private List<String> createdCouponCodes;

    @NotBlank
    private String couponGroupCode;
}
