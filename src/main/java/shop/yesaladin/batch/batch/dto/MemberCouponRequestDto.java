package shop.yesaladin.batch.batch.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberCouponRequestDto {

    private MemberDto memberDto;
    private List<String> couponCodes = new ArrayList<>();
    private List<String> groupCodes = new ArrayList<>();

    public MemberCouponRequestDto(MemberDto memberDto) {
        this.memberDto = memberDto;
    }
}
