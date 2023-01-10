package shop.yesaladin.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MemberPaymentDto {

    private Long memberId;
    private Long totalAmount;
    private Long cancelAmount;

}
