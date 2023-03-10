package shop.yesaladin.batch.order.mapper;

import org.springframework.jdbc.core.RowMapper;
import shop.yesaladin.batch.order.dto.OrderStatusChangeLogDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;


/**
 * DB 에서 조회한 주문 상태 변경 이력을 OrderStatusChangeLogDto 로 매핑하기 위한 RowMapper 구현체입니다.
 *
 * @author 이수정
 * @since 1.0
 */
public class OrderStatusChangeLogDtoRowMapper implements RowMapper<OrderStatusChangeLogDto> {

    @Override
    public OrderStatusChangeLogDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        OrderStatusChangeLogDto orderStatusChangeLogDto = new OrderStatusChangeLogDto();

        orderStatusChangeLogDto.setOrderId(rs.getLong("order_id"));
        orderStatusChangeLogDto.setChangeDateTime(rs.getObject("change_datetime", LocalDateTime.class));
        orderStatusChangeLogDto.setOrderStatusCodeId(rs.getInt("order_status_code_id"));

        return orderStatusChangeLogDto;
    }

}
