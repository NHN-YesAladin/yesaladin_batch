package shop.yesaladin.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import shop.yesaladin.batch.config.JobConfig;

@EnableScheduling
@SpringBootApplication
public class YesaladinBatchApplication {

    public static void main(String[] args) {
//        SpringApplication.run(YesaladinBatchApplication.class, args);
        SpringApplication.run(JobConfig.class, args);
    }

}
