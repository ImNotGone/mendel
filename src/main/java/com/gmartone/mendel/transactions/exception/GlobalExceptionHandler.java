package com.gmartone.mendel.transactions.exception;

import com.gmartone.mendel.transactions.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Centralises exception-to-HTTP-response mapping so that controllers stay free
 * of try/catch blocks and every error always returns a consistent
 * {@link ErrorResponse} body.
 *
 * <p>Handled exceptions and their HTTP status codes:
 * <ul>
 *   <li>{@link TransactionNotFoundException} → 404 Not Found</li>
 *   <li>{@link IllegalArgumentException} → 400 Bad Request</li>
 *   <li>{@link MethodArgumentNotValidException} → 400 Bad Request (bean validation)</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TransactionNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(ex.getMessage()));
    }

    /**
     * Collects all field-level constraint violations from {@code @Valid} into a
     * single, comma-separated message so the caller knows exactly what is wrong.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(message));
    }
}
