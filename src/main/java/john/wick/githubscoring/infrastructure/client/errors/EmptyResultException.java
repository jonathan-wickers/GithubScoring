package john.wick.githubscoring.infrastructure.client.errors;

public class EmptyResultException extends RuntimeException {

    public EmptyResultException(String message) {
        super(message);
    }
}
