package shop.yesaladin.batch.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ServerMetaConfig {

    @Value("${yesaladin.shop.url}")
    private String shopServerUrl;

    @Value("${yesaladin.coupon.url}")
    private String couponServerUrl;
}
