package john.wick.githubscoring.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GitHub Repository Scoring API")
                        .description("API for searching and scoring GitHub repositories based on stars, forks and recency of updates")
                        .version("1.0.0")
                );
    }
}

