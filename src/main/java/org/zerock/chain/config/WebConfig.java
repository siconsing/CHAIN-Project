package org.zerock.chain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration  // 이 클래스가 스프링의 설정 클래스임을 나타냄.
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // "/upload/**" 경로로 들어오는 모든 요청을 C:/upload/ 폴더에서 리소스를 서빙하도록 설정
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:///C:/upload/");

        // "/assets/img/mailimg/**" 경로로 들어오는 요청도 동일하게 C:/upload/ 폴더에서 서빙하도록 설정
        registry.addResourceHandler("/assets/img/mailimg/**")
                .addResourceLocations("file:///C:/upload/");
    }
}