package com.m1sigl.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Mappe l'URL http://localhost:8080/files/image.jpg
        // vers le dossier physique "uploads/" à la racine du projet
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:uploads/");
    }
}