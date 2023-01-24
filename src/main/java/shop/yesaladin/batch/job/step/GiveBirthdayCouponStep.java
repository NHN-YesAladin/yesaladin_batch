package shop.yesaladin.batch.job.step;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class GiveBirthdayCouponStep {

    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;



}
