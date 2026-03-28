package com.br.capoeira.eventos.evento_api.config.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalHandlerException {

    @ExceptionHandler(FileException.class)
    public ResponseEntity<StandardException> file(FileException e, HttpServletRequest request) {
        var err = new StandardException(System.currentTimeMillis(), HttpStatus.BAD_REQUEST.value(), "Error on S3 file", e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<StandardException> validation(FileException e, HttpServletRequest request) {

        var err = new StandardException(System.currentTimeMillis(), HttpStatus.BAD_REQUEST.value(), "Error on trying to validate attributes", e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }
}
