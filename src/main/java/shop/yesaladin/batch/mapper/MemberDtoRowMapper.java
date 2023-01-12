package shop.yesaladin.batch.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import shop.yesaladin.batch.dto.MemberDto;

/**
 *
 * @author 서민지
 * @since  1.0
 */
public class MemberDtoRowMapper implements RowMapper<MemberDto> {

    @Override
    public MemberDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        MemberDto memberDto = new MemberDto();

        memberDto.setMemberId(rs.getLong("member_id"));
        memberDto.setMemberGradeId(rs.getInt("member_grade_id"));
        memberDto.setPoint(rs.getLong("point"));

        Long totalAmount = rs.getLong("total_amount");
        Long cancelAmount = rs.getLong("cancel_amount");
        memberDto.setTotalPaymentAmount(totalAmount - cancelAmount);

        return memberDto;
    }
}
