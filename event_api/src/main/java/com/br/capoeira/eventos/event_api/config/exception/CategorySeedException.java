package com.br.capoeira.eventos.event_api.config.exception;

public class CategorySeedException extends RuntimeException {

    public CategorySeedException(String message) {
        super(message);
    }

    public CategorySeedException(String message, Throwable cause) {
        super(message, cause);
    }
}
