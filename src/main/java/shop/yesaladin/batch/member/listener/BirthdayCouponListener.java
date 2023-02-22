package shop.yesaladin.batch.member.listener;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.annotation.OnProcessError;
import org.springframework.batch.core.annotation.OnReadError;
import org.springframework.batch.core.annotation.OnWriteError;
import org.springframework.stereotype.Component;

/**
 * BirthdayCouponStep 의 ItemReader, ItemProcessor, ItemWriter 에서 발생하는 에러의 로그를 작성하기 위한 리스너 클래스입니다.
 *
 * @author 서민지
 * @since 1.0
 */
@Slf4j
@Component
public class BirthdayCouponListener {

    /**
     * ItemReader 에서 발생한 에러 메시지를 로그로 작성합니다.
     *
     * @param e 발생한 예외
     */
    @OnReadError
    public void onReadError(Exception e) {
        log.error("[BirthdayCouponStep] ItemReader error message", e);
    }

    /**
     * ItemProcessor 에서 발생한 에러 메시지를 로그로 작성합니다.
     *
     * @param item 예외가 발생한 item
     * @param e    발생한 예외
     */
    @OnProcessError
    public void onProcessError(Object item, Exception e) {
        log.error("[BirthdayCouponStep] ItemProcessor error message", e);
    }

    /**
     * ItemWriter 에서 발생한 에러 메시지를 로그로 작성합니다.
     *
     * @param e     발생한 예외
     * @param items 예외가 발생한 item list
     */
    @OnWriteError
    public void onWriteError(Exception e, List<Object> items) {
        log.error("[BirthdayCouponStep] ItemWriter error message", e);
    }
}
