package com.aditya.solarmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Aditya Solar Management API", version = "v1"))
public class SolarManagementApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolarManagementApiApplication.class, args);
	}

}
