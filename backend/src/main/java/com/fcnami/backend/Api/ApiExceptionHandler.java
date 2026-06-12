package com.fcnami.backend.Api;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * ApiExceptionHandler handles exceptions thrown by the API controllers globally.
 * It maps various exception types to appropriate HTTP status codes and standard error responses.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Handles EntityNotFoundException by returning a NOT_FOUND status.
     *
     * Precondition: An EntityNotFoundException must be thrown.
     * Postcondition: A standard error map is returned with a 404 status.
     * Side-effect: None.
     *
     * @param exception the caught EntityNotFoundException
     * @return a map containing the error details
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> notFound(EntityNotFoundException exception) {
        return error("not_found", exception.getMessage());
    }

    /**
     * Handles MethodArgumentNotValidException by returning a BAD_REQUEST status.
     *
     * Precondition: A MethodArgumentNotValidException must be thrown (e.g., failed validation).
     * Postcondition: A validation error response is returned listing all field errors.
     * Side-effect: None.
     *
     * @param exception the caught MethodArgumentNotValidException
     * @return a map containing the error details and field validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> validation(MethodArgumentNotValidException exception) {
        return error("validation_error", exception.getBindingResult().getFieldErrors().stream()
                .map(field -> field.getField() + " " + field.getDefaultMessage())
                .toList());
    }

    /**
     * Handles RuntimeException by returning a BAD_REQUEST status.
     *
     * Precondition: A RuntimeException must be thrown.
     * Postcondition: A standard bad request error response is returned with the exception message.
     * Side-effect: None.
     *
     * @param exception the caught RuntimeException
     * @return a map containing the error details
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> runtime(RuntimeException exception) {
        return error("bad_request", exception.getMessage());
    }

    /**
     * Handles any unhandled general Exception by returning an INTERNAL_SERVER_ERROR status.
     *
     * Precondition: An unhandled general Exception must be thrown.
     * Postcondition: A standard unexpected internal error response is returned.
     * Side-effect: None.
     *
     * @param exception the caught general Exception
     * @return a map containing the generic error details
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> fallback(Exception exception) {
        return error("internal_error", "Unexpected server error");
    }

    /**
     * Helper method to construct a standardized API error response.
     *
     * Precondition: code and message must not be null.
     * Postcondition: An immutable map representing the standard API error structure is created.
     * Side-effect: Generates a random UUID to identify the specific error occurrence.
     *
     * @param code the error classification code
     * @param message the detailed error message or list of errors
     * @return the standardized error payload map
     */
    private Map<String, Object> error(String code, Object message) {
        return Map.of(
                "timestamp", Instant.now(),
                "errorId", UUID.randomUUID().toString(),
                "code", code,
                "message", message
        );
    }
}

