package shop.yesaladin.batch.batch.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;

/**
 * 요청한 쿠폰의 생성된 쿠폰코드와 그룹코드, 만료일 정보를 담은 dto 입니다.
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
    private LocalDate expirationDate;
}
