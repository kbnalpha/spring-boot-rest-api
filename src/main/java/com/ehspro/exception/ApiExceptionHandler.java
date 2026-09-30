package com.ehspro.exception;

import com.ehspro.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @InitBinder
    public void directFieldValidation(org.springframework.web.bind.WebDataBinder binder) {
        // DTOs expose public fields. Nested validation paths must use the same access strategy.
        binder.initDirectFieldAccess();
    }
    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> api(ApiException e) { return error(e.status, e.getMessage()); }
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> denied(Exception e) { return error(HttpStatus.FORBIDDEN, e.getMessage()); }
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> authentication(Exception e) {return error(HttpStatus.UNAUTHORIZED,"Invalid credentials or account unavailable");}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage()).distinct().sorted()
            .collect(java.util.stream.Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, message);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiResponse<Void>> malformed(Exception e) { return error(HttpStatus.BAD_REQUEST, "Invalid request body or field value"); }
    @ExceptionHandler({DataIntegrityViolationException.class, org.hibernate.exception.ConstraintViolationException.class})
    public ResponseEntity<ApiResponse<Void>> conflict(Exception e) { return error(HttpStatus.CONFLICT, "A duplicate record or invalid reference was supplied"); }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> missing(Exception e) { return error(HttpStatus.NOT_FOUND, "Endpoint not found"); }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> method(Exception e) { return error(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not supported"); }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> unexpected(Exception e) {
        LOG.error("Unexpected API failure", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
    private ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiResponse<>(status.value(), message, null));
    }
}
