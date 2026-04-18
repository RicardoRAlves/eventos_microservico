package com.br.capoeira.eventos.organization_api;

import org.springframework.boot.SpringApplication;

public class TestOrganizationApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(OrganizationApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
