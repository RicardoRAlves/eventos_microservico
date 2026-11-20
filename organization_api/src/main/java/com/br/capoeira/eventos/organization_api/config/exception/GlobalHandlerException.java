package com.br.capoeira.eventos.organization_api.config.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalHandlerException {

    private static final String ORGANIZATION_KEY_UNIQUE_CONSTRAINT =
            "uk_organization_organization_key";

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

    @ExceptionHandler(OrganizationKeyConflictException.class)
    public ResponseEntity<StandardException> organizationKeyConflict(
            OrganizationKeyConflictException e,
            HttpServletRequest request) {

        return organizationKeyConflictResponse(e.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardException> dataIntegrityViolation(
            DataIntegrityViolationException e,
            HttpServletRequest request) {

        if (isOrganizationKeyUniqueConstraintViolation(e)) {
            return organizationKeyConflictResponse(
                    OrganizationKeyConflictException.DEFAULT_MESSAGE,
                    request
            );
        }

        log.error("Unexpected data integrity violation at path={}", request.getRequestURI(), e);

        var err = new StandardException(
                System.currentTimeMillis(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "DATA_INTEGRITY_ERROR",
                "Não foi possível persistir os dados.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }

    private ResponseEntity<StandardException> organizationKeyConflictResponse(
            String message,
            HttpServletRequest request) {

        var err = new StandardException(
                System.currentTimeMillis(),
                HttpStatus.CONFLICT.value(),
                OrganizationKeyConflictException.ERROR_CODE,
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    private boolean isOrganizationKeyUniqueConstraintViolation(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof ConstraintViolationException constraintViolation
                    && ORGANIZATION_KEY_UNIQUE_CONSTRAINT.equals(constraintViolation.getConstraintName())) {
                return true;
            }

            current = current.getCause();
        }

        Throwable mostSpecificCause = throwable;
        if (throwable instanceof DataIntegrityViolationException dataIntegrityViolation) {
            mostSpecificCause = dataIntegrityViolation.getMostSpecificCause();
        }

        return mostSpecificCause.getMessage() != null
                && mostSpecificCause.getMessage().contains(ORGANIZATION_KEY_UNIQUE_CONSTRAINT);
    }
}
