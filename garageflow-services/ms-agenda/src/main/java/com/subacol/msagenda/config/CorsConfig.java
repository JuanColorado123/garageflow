package com.subacol.msagenda.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private final String corsOrigin;

    public CorsConfig(@Value("${app.cors.origen}") String corsOrigin) {
        this.corsOrigin = corsOrigin;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(corsOrigin)
                .allowedMethods("GET", "POST")
                .allowedHeaders("*");
    }
}
