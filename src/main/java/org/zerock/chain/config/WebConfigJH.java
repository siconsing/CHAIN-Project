package org.zerock.chain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfigJH implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /uploads/** 경로의 요청을 src/main/resources/static/uploads/로 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + Paths.get("src/main/resources/static/uploads/").toAbsolutePath().toString() + "/");
    }
}