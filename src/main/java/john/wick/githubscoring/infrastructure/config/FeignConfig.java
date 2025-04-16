package john.wick.githubscoring.infrastructure.config;

import feign.Logger;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import john.wick.githubscoring.infrastructure.client.errors.CustomErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.concurrent.TimeUnit.SECONDS;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(1000, SECONDS.toMillis(1), 3);
    }


    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}