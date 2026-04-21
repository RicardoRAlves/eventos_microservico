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
    public ResponseEntity<StandardException> validation(ValidationException e, HttpServletRequest request) {

        HttpStatus status = e.getMessage() != null &&
                e.getMessage().contains("not found")
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
    public ResponseEntity<StandardException> validation(FileException e, HttpServletRequest request) {

        HttpStatus status = e.getMessage() != null &&
                e.getMessage().contains("not found")
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardException> methodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

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
            MissingServletRequestPartException ex,
            HttpServletRequest request
    ) {
        StandardException error = new StandardException(
                System.currentTimeMillis(),
                HttpStatus.BAD_REQUEST.value(),
                "Image file is required",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }
}
