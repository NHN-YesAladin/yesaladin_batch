package shop.yesaladin.batch.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 주문 상태 변경 이력 정보를 담은 OrderStatusChangeLogInsertStep 에서 사용된 Item 의 Dto 입니다.
 *
 * @author 이수정
 * @since 1.0
 */
@Getter
@Setter
@NoArgsConstructor
public class OrderStatusChangeLogDto {

    private long orderId;
    private LocalDateTime changeDateTime;
    private int orderStatusCodeId;
}
