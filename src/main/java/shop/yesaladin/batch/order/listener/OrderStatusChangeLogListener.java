package shop.yesaladin.batch.order.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.annotation.OnReadError;
import org.springframework.batch.core.annotation.OnWriteError;
import org.springframework.stereotype.Component;
import shop.yesaladin.batch.order.dto.OrderStatusChangeLogDto;

import java.util.List;

/**
 * orderStatusChangeLogListener 실행 시 생기는 예외에 대해 로그를 작성하는 Listener 입니다.
 *
 * @author 이수정
 * @since 1.0
 */
@Slf4j
@Component
public class OrderStatusChangeLogListener {

    /**
     * Reader 예외 발생 시 로그를 작성합니다.
     *
     * @param ex 발생한 예외
     * @author 이수정
     * @since 1.0
     */
    @OnReadError
    public void onReadError(Exception ex) {
        log.error(
                "Job = insertOrderStatusChangeLogJob, " +
                        "Step = orderStatusChangeLogInsertStep, " +
                        "Reader = orderStatusChangeLogItemReader" +
                        "Error = " + ex
        );
    }

    /**
     * Writer 예외 발생 시 로그를 작성합니다.
     *
     * @param ex 발생한 예외
     * @author 이수정
     * @since 1.0
     */
    @OnWriteError
    public void onWriteError(Exception ex, List<OrderStatusChangeLogDto> items) {
        log.error(
                "Job = insertOrderStatusChangeLogJob, " +
                        "Step = orderStatusChangeLogInsertStep, " +
                        "Writer = orderStatusChangeLogItemWriter" +
                        "Error = " + ex + ", Items = " + items
        );
    }
}
