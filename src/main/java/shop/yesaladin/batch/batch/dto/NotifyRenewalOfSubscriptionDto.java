package shop.yesaladin.batch.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 구독 갱신 알람을 위한 정보를 담은 RenewalOfSubscriptionNotifyStep 에서 사용되는 Item 의 Dto 입니다.
 *
 * @author 이수정
 * @since 1.0
 */
@Getter
@Setter
@NoArgsConstructor
public class NotifyRenewalOfSubscriptionDto {

    private String title;
    private String loginId;
    private String name;
    private LocalDate nextRenewalDate;
    private int intervalMonth;
}
