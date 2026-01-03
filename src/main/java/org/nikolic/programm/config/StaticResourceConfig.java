package org.nikolic.programm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ✅ COMPLETE FIX - Forces HTML to be served correctly
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static resources
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(false);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // ✅ CRITICAL: Tell Spring how to handle different file types
        configurer
                .favorPathExtension(false)
                .favorParameter(false)
                .ignoreAcceptHeader(false)
                .defaultContentType(MediaType.TEXT_HTML)  // Default to HTML
                .mediaType("html", MediaType.TEXT_HTML)
                .mediaType("json", MediaType.APPLICATION_JSON)
                .mediaType("xml", MediaType.APPLICATION_XML)
                .mediaType("js", MediaType.valueOf("application/javascript"))
                .mediaType("css", MediaType.valueOf("text/css"));
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Map root to index.html
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}