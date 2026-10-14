package com.campusconnect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralized Web MVC and CORS configuration for local development and API access.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:5174,http://localhost:3000,https://ai-powered-campus-connect-platform.vercel.app}")
    private String allowedOrigins;

    private List<String> getOriginsList() {
        return Arrays.stream(allowedOrigins.split(","))
                     .map(String::trim)
                     .filter(s -> !s.isEmpty())
                     .map(origin -> origin.replaceAll("/+$", ""))
                     .distinct()
                     .collect(Collectors.toList());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = getOriginsList();
        registry.addMapping("/**")
                .allowedOrigins(origins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers")
                .exposedHeaders("Authorization", "Content-Disposition")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = getOriginsList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedOriginPatterns(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers", "*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
