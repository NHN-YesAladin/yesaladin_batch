package shop.yesaladin.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author 서민지
 * @since  1.0
 */
@Getter
@Setter
@NoArgsConstructor
public class MemberDto {

    private Long memberId;
    private Integer memberGradeId;
    private Long point;
    private Long totalPaymentAmount;

    public void updateMemberGrade(int memberGradeId) {
        this.memberGradeId = memberGradeId;
    }

    public void addPoint(long point) {
        this.point += point;
    }
}