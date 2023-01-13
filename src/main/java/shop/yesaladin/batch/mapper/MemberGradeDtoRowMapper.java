package shop.yesaladin.batch.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import shop.yesaladin.batch.dto.MemberGradeDto;

/**
 * 데이터베이스에서 조회한 회원 데이터를 MemberDto 클래스에 매핑하기 위한 RowMapper 입니다.
 *
 * @author 서민지
 * @since  1.0
 */
public class MemberGradeDtoRowMapper implements RowMapper<MemberGradeDto> {

    @Override
    public MemberGradeDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        MemberGradeDto memberGradeDto = new MemberGradeDto();

        memberGradeDto.setMemberId(rs.getLong("member_id"));
//        Long totalAmount = rs.getLong("total_amount");
//        Long cancelAmount = rs.getLong("cancel_amount");
//        memberDto.setPayAmount(totalAmount - cancelAmount);
        memberGradeDto.setPayAmount(rs.getLong("pay_amount"));

        return memberGradeDto;
    }
}
