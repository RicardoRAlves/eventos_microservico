package com.br.capoeira.eventos.evento_api.config.exception;

public class ValidationException extends RuntimeException {
    public ValidationException (String msg){
        super(msg);
    }
}
