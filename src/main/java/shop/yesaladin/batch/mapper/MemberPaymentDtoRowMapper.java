package shop.yesaladin.batch.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import shop.yesaladin.batch.dto.MemberPaymentDto;

public class MemberPaymentDtoRowMapper implements RowMapper<MemberPaymentDto> {

    @Override
    public MemberPaymentDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        MemberPaymentDto memberPaymentDto = new MemberPaymentDto();
        memberPaymentDto.setMemberId(rs.getLong("member_id"));

        Long orderAmount = rs.getLong("order_amount");
        Long cancelAmount = rs.getLong("cancel_amount");
        memberPaymentDto.setTotalPaymentAmount(orderAmount - cancelAmount);

        return memberPaymentDto;
    }
}
