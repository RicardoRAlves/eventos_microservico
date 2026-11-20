package com.br.capoeira.eventos.organization_api.config.exception;

public class BonfimSeedException extends RuntimeException {

    public BonfimSeedException(String message) {
        super(message);
    }

    public BonfimSeedException(String message, Throwable cause) {
        super(message, cause);
    }
}
