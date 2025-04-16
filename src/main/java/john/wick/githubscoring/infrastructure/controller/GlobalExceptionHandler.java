package john.wick.githubscoring.infrastructure.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import john.wick.githubscoring.infrastructure.client.errors.EmptyResultException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static String getFieldNaem(ConstraintViolation<?> violation) {
        String field = violation.getPropertyPath().toString();
        field = field.substring(field.lastIndexOf('.') + 1);
        return field;
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<String> handleDateTimeParseException(DateTimeParseException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(STR."Invalid date format: \{ex.getParsedString()}");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(STR."An unexpected error occurred: \{ex.getMessage()}");
    }

    @ExceptionHandler(EmptyResultException.class)
    public ResponseEntity<String> handleEmptyResultsException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("Github returned no results for your search.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(STR."Rate limit exceeded: \{ex.getReason()}");
        }
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> responseBody = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        ex.getConstraintViolations().forEach(violation -> {
            String field = getFieldNaem(violation);
            errors.put(field, violation.getMessage());

        });

        responseBody.put("status", HttpStatus.BAD_REQUEST.value());
        responseBody.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }


}
