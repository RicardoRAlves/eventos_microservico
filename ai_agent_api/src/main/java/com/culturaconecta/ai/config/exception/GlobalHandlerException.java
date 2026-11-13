package com.culturaconecta.ai.config.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalHandlerException {

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<StandardException> handleServiceUnavailable(
            ServiceUnavailableException e,
            HttpServletRequest request) {

        log.error("AI provider unavailable", e);

        var error = new StandardException(
                System.currentTimeMillis(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "External service unavailable",
                request.getRequestURI(),
                "AI service is temporarily unavailable"
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardException> handleValidation(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining(", "));

        var error = new StandardException(
                System.currentTimeMillis(),
                HttpStatus.BAD_REQUEST.value(),
                "Error on trying to validate attributes",
                request.getRequestURI(),
                message
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardException> handleGeneric(
            Exception e,
            HttpServletRequest request) {

        log.error("Unexpected AI API error", e);

        var error = new StandardException(
                System.currentTimeMillis(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Unexpected internal error",
                request.getRequestURI(),
                "An unexpected error occurred"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}

