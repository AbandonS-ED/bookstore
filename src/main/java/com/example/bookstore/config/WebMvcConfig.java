package com.example.bookstore.config;

import com.example.bookstore.interceptor.AdminInterceptor;
import com.example.bookstore.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/user/info", "/api/user/password", "/api/user/profile",
                        "/api/cart/**", "/api/order/**", "/api/review/add");

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**");
    }
}