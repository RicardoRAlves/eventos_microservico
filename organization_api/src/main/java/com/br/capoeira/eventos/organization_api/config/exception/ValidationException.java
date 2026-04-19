package com.br.capoeira.eventos.organization_api.config.exception;

public class ValidationException extends RuntimeException {
    public ValidationException (String msg){
        super(msg);
    }
}
