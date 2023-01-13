package shop.yesaladin.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원 정보를 담은 MemberUpdateStep 에서 사용될 item 단위입니다.
 *
 * @author 서민지
 * @since  1.0
 */
@Getter
@Setter
@NoArgsConstructor
public class MemberPointDto {

    private Long memberId;
    private Long memberGradePoint;
}