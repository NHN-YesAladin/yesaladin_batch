package shop.yesaladin.batch.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Shop, Coupon 서버의 정보를 담는 Configuration 입니다.
 *
 * @author 서민지
 * @since 1.0
 */
@Getter
@Configuration
public class ServerMetaConfig {

    @Value("${yesaladin.shop.url}")
    private String shopServerUrl;

    @Value("${yesaladin.coupon.url}")
    private String couponServerUrl;
}
