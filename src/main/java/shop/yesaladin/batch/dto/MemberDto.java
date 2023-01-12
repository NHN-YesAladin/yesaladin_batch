package shop.yesaladin.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
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