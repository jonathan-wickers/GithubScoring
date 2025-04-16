package john.wick.githubscoring.infrastructure.client.errors;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

public class CustomErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());

        switch (status) {
            case NOT_FOUND:
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
            case BAD_REQUEST:
                return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request made");
            case INTERNAL_SERVER_ERROR:
                return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "The server encountered an error");
            case TOO_MANY_REQUESTS:
                handleRateLimit(response);
            case FORBIDDEN:
                handleRateLimit(response);
            default:
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }

    private String getHeader(Response response, String headerName) {
        Collection<String> headerValues = response.headers().get(headerName);
        return (headerValues != null && !headerValues.isEmpty()) ? headerValues.iterator().next() : null;
    }

    private void handleRateLimit(Response response) {
        String retryAfter = getHeader(response, "Retry-After");
        String resetTime = getHeader(response, "X-RateLimit-Reset");

        String errorMessage = "Rate limit exceeded.";
        if (retryAfter != null) {
            errorMessage += "Retry after " + retryAfter + " seconds.";
        } else if (resetTime != null) {
            errorMessage += "Rate limit resets at " + resetTime + ".";
        } else {
            errorMessage += "Please try again later.";
        }

        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, errorMessage);
    }
}