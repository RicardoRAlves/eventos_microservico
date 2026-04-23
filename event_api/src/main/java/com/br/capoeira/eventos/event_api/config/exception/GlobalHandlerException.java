package com.br.capoeira.eventos.event_api.config.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalHandlerException {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<StandardException> handleValidationException(
            ValidationException e,
            HttpServletRequest request
    ) {
        HttpStatus status = e.getMessage() != null &&
                e.getMessage().toLowerCase().contains("not found")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;

        var err = new StandardException(
                System.currentTimeMillis(),
                status.value(),
                "Error on trying to validate attributes",
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(FileException.class)
    public ResponseEntity<StandardException> handleFileException(
            FileException e,
            HttpServletRequest request
    ) {
        HttpStatus status = e.getMessage() != null &&
                e.getMessage().toLowerCase().contains("not found")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;

        var err = new StandardException(
                System.currentTimeMillis(),
                status.value(),
                "Error on trying to update file to S3 bucket",
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<StandardException> handleServiceUnavailableException(
            ServiceUnavailableException e,
            HttpServletRequest request
    ) {
        var err = new StandardException(
                System.currentTimeMillis(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "External service unavailable",
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardException> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining(", "));

        var err = new StandardException(
                System.currentTimeMillis(),
                HttpStatus.BAD_REQUEST.value(),
                "Error on trying to validate attributes",
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<StandardException> handleMissingServletRequestPartException(
            MissingServletRequestPartException e,
            HttpServletRequest request
    ) {
        var err = new StandardException(
                System.currentTimeMillis(),
                HttpStatus.BAD_REQUEST.value(),
                "Image file is required",
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardException> handleGenericException(
            Exception e,
            HttpServletRequest request
    ) {
        var err = new StandardException(
                System.currentTimeMillis(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Unexpected internal error",
                e.getMessage() != null ? e.getMessage() : "Unexpected error",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}
