package shop.yesaladin.batch.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원 정보를 담은 MemberGradeUpdateStep 에서 사용될 item 단위입니다.
 *
 * @author 서민지
 * @since 1.0
 */
@Getter
@Setter
@NoArgsConstructor
public class MemberGradeDto {

    private Long memberId;
    private Long payAmount;
    private Integer memberGradeId;

    public void updateMemberGrade(int memberGradeId) {
        this.memberGradeId = memberGradeId;
    }
}