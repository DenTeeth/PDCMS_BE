package com.dental.clinic.management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for resource handling.
 * 
 * This configuration ensures that static resources are served only from
 * designated paths and do NOT interfere with REST API endpoints.
 * 
 * Problem it solves:
 * - Spring Boot's default ResourceHttpRequestHandler uses pattern "/**"
 * - This causes API paths like /api/v1/invoices/patient-history/{code} 
 *   to be incorrectly mapped to static resource handler instead of controllers
 * - Result: NoResourceFoundException instead of controller method execution
 * 
 * Solution:
 * - Explicitly configure resource handlers with specific patterns
 * - Disable default "/**" pattern that conflicts with API routes
 * - Use setResourceChain(false) to prevent caching issues
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Disable default /** resource handler by not calling super.addResourceHandlers()
        // and explicitly defining only what we need
        
        // Serve static resources only from /static/** path
        // Maps requests like /static/css/style.css to classpath:/static/css/style.css
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600) // 1 hour cache
                .resourceChain(false); // Disable resource chain to prevent caching conflicts
        
        // Serve uploaded files from /uploads/** path (if needed)
        // Uncomment if you serve user-uploaded files
        // registry.addResourceHandler("/uploads/**")
        //         .addResourceLocations("file:./uploads/")
        //         .setCachePeriod(3600)
        //         .resourceChain(false);
        
        // Explicitly do NOT add "/**" pattern - this is the key fix
        // API endpoints under /api/** will now be handled by controllers, not resource handler
    }
}
