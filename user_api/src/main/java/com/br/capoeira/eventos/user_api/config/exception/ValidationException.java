package com.br.capoeira.eventos.user_api.config.exception;

public class ValidationException extends RuntimeException {
    public ValidationException (String msg){
        super(msg);
    }
}
