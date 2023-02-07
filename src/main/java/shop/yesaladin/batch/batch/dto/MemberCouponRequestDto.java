package shop.yesaladin.batch.batch.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Shop 서버에 회원 쿠폰 지급 요청을 위한 정보를 담은 dto 입니다. 쿠폰 코드와 그룹 코드는 리스트 내에서 동일한 순서로 한 종류의 쿠폰을 나타냅니다.
 *
 * @author 서민지
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public class MemberCouponRequestDto {

    private Long memberId;
    private List<String> couponCodes = new ArrayList<>();
    private List<String> couponGroupCodes = new ArrayList<>();

    public MemberCouponRequestDto(Long memberId) {
        this.memberId = memberId;
    }
}
