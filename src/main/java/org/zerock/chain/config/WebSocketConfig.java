package org.zerock.chain.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.*;

import java.util.List;


@Configuration
@EnableWebSocketMessageBroker //웹 소켓 메시지를 다룰 수 있게 허용(STOMP를 사용하기 위한 어노테이션)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 메시지 브로커 구성을 설정하는 메서드
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/user", "/topic");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS(); // setAllowedOrigins("*")
    }

    // 메시지 번환기
    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        // 기본 콘텐츠 타입 설정
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);

        // Jackson 메시지 변환기 생성 및 ObjectMapper 설정
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Java 8 날짜/시간 모듈 등록

        converter.setObjectMapper(objectMapper); // ObjectMapper 설정
        converter.setContentTypeResolver(resolver); // MIME 타입 설정

        // 변환기 리스트에 추가
        messageConverters.add(converter);
        return false;
    }
}
