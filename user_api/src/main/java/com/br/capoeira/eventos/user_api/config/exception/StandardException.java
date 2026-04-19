package com.br.capoeira.eventos.user_api.config.exception;

public record StandardException(
        Long timestamp,
        Integer status,
        String error,
        String message,
        String path
) {}
