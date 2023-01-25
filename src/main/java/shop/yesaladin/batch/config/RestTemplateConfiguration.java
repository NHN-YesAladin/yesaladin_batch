package shop.yesaladin.batch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

    // TODO restTemplate config 설정
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
