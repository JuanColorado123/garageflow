package com.subacol.msauditoria.config;

import com.subacol.msauditoria.adapter.in.web.RolInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de Spring MVC para registrar el interceptor de roles.
 */
@Configuration
public class RolConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RolInterceptor())
                .addPathPatterns("/api/**");
    }
}
