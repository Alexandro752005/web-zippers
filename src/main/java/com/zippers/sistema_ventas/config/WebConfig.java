package com.zippers.sistema_ventas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 1. Calculamos la ruta física exacta y absoluta en tu disco duro (Windows/Mac/Linux)
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toAbsolutePath().toUri().toString();

        // 2. Le decimos a Spring Boot que conecte la URL "/uploads/..." con esa ruta absoluta
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}