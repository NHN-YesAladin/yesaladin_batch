package shop.yesaladin.batch.batchstep;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class AllMemberGradeResetStep {

    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

//    @Bean
    public Step resetAllMemberGradeStep() {
        return stepBuilderFactory
                .get("resetAllMemberGradeStep")
                .chunk(10)
                .build();
    }
}
