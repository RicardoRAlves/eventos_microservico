package com.br.capoeira.eventos.organization_api.config.exception;

public record StandardException(
        Long timestamp,
        Integer status,
        String error,
        String message,
        String path
) {}
