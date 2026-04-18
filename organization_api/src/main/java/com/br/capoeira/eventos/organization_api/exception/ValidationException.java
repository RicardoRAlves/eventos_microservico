package com.br.capoeira.eventos.organization_api.exception;

public class ValidationException extends RuntimeException {
    public ValidationException (String msg){
        super(msg);
    }
}
