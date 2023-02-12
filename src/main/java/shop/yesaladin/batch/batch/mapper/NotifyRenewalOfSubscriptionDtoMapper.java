package shop.yesaladin.batch.batch.mapper;

import org.springframework.jdbc.core.RowMapper;
import shop.yesaladin.batch.batch.dto.NotifyRenewalOfSubscriptionDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * DB 에서 조회한 구독 갱신 알림을 위한 정보를 NotifyRenewalOfSubscriptionDto 로 매핑하기 위한 RowMapper 구현체입니다.
 *
 * @author 이수정
 * @since 1.0
 */
public class NotifyRenewalOfSubscriptionDtoMapper implements RowMapper<NotifyRenewalOfSubscriptionDto> {

    @Override
    public NotifyRenewalOfSubscriptionDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        NotifyRenewalOfSubscriptionDto notifyRenewalOfSubscriptionDto = new NotifyRenewalOfSubscriptionDto();

        notifyRenewalOfSubscriptionDto.setTitle(rs.getString("title"));
        notifyRenewalOfSubscriptionDto.setLoginId(rs.getString("login_id"));
        notifyRenewalOfSubscriptionDto.setName(rs.getString("name"));
        notifyRenewalOfSubscriptionDto.setNextRenewalDate(rs.getObject("next_renewal_date", LocalDate.class));
        notifyRenewalOfSubscriptionDto.setIntervalMonth(rs.getInt("interval_month"));

        return notifyRenewalOfSubscriptionDto;
    }
}
