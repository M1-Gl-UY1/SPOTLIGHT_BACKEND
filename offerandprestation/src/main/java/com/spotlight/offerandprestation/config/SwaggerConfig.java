package com.spotlight.offerandprestation.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spotlight - API Offres & Prestations")
                        .version("1.0")
                        .description("Microservice gérant le catalogue de services, les packs et le cycle de vie des commandes."));
    }
}
