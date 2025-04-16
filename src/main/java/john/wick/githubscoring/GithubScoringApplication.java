package john.wick.githubscoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "john.wick.githubscoring.infrastructure.client")
public class GithubScoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(GithubScoringApplication.class, args);
    }

}
