package john.wick.githubscoring.infrastructure.client.errors;

public class RateLimitException extends ClientException {

    public RateLimitException(String message) {
        super(message);
    }

}
