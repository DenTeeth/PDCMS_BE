package com.dental.clinic.management.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Production vÃƒÂ  Development origins
                configuration.setAllowedOrigins(Arrays.asList(
                                "http://localhost:3000" // Development
                // "https://yourdomain.com", // Production
                // "https://www.yourdomain.com" // Production www
                ));

                configuration.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

                configuration.setAllowedHeaders(Arrays.asList(
                                "Authorization",
                                "Content-Type",
                                "Accept",
                                "X-Requested-With",
                                "Origin"));

                configuration.setExposedHeaders(Arrays.asList(
                                "Authorization",
                                "Set-Cookie" // Cho phÃƒÂ©p frontend Ã„â€˜Ã¡Â»Âc Set-Cookie
                ));

                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
