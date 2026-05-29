package com.example.bookstore.config;

import com.example.bookstore.interceptor.AdminInterceptor;
import com.example.bookstore.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/user/info", "/api/user/password", "/api/user/profile",
                        "/api/address/**",
                        "/api/cart/**", "/api/favorite/**", "/api/order/**", "/api/review/add",
                        "/api/community/add", "/api/community/update", "/api/community/like/**",
                        "/admin/**");

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String picturesPath = Paths.get("pictures").toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/pictures/**")
                .addResourceLocations(picturesPath);
    }
}