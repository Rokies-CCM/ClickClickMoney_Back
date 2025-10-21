package com.click.click.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * WebClient를 쓰는 방법이 가장 이상적이지만(비동기/Backpressure),
 * 현재 의존성 변경 없이 바로 붙이기 위해 RestTemplate 구성 유지.
 * timeout만 적절히 설정.
 */
@Configuration
public class PyConfig {

    @Value("${chatbot.base-url:http://localhost:8000}")
    private String chatbotBaseUrl;

    @Bean
    public RestTemplate restTemplate() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(15000);
        return new RestTemplate(factory);
    }
}
