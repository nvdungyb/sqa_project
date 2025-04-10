package com.java.be_ticket_booking.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition
@Configuration
public class SwaggerConfig {
    
    @Bean
	public OpenAPI baseOpenAPI() {
		return new OpenAPI().info(
                            new Info()
                                .title("Rest API Guidlines")
                                .description("This is a document to demonstrate how backend work.")
                                .version("1.0.0")
                            );
	}
}
