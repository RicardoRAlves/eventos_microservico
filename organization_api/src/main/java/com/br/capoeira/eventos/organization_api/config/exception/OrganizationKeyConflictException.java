package com.br.capoeira.eventos.organization_api.config.exception;

public class OrganizationKeyConflictException extends RuntimeException {

    public static final String ERROR_CODE = "ORGANIZATION_KEY_ALREADY_EXISTS";
    public static final String DEFAULT_MESSAGE = "Já existe uma organização cadastrada com este nome.";

    public OrganizationKeyConflictException() {
        super(DEFAULT_MESSAGE);
    }
}
