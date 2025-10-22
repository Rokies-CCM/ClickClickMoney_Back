package com.click.click.ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final RestTemplate restTemplate;

    @Value("${chatbot.base-url:http://localhost:8000}")
    private String pythonServiceBaseUrl;

    /**
     * Spring → FastAPI 프록시 (운영에서 한 도메인으로 묶고 싶을 때 사용)
     * 프런트는 /api/ai/chat 으로 호출하면 Spring이 FastAPI /v1/chat으로 중계.
     */
    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody Map<String, Object> body) {
        if (!body.containsKey("question")) {
            return ResponseEntity
                    .badRequest()
                    .body("{\"error\":\"'question' field is required\"}");
        }

        String url = pythonServiceBaseUrl + "/v1/chat";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> res = restTemplate.postForEntity(url, entity, String.class);
            return ResponseEntity.status(res.getStatusCode()).body(res.getBody());
        } catch (Exception e) {
            // 다운/연결 실패 등
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("{\"error\":\"chatbot upstream unavailable\"}");
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("{\"ok\":true}");
    }
}