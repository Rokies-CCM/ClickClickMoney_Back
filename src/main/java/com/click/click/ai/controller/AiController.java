package com.click.click.ai.controller;

import com.click.click.consumption.dto.ConsumptionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final RestTemplate restTemplate;

    @Value("")
    private String pythonServiceUrl;

    @PostMapping("/llm")
    public ResponseEntity<ConsumptionDTO.ConsumptionResponse> consumption(
            @Validated @RequestBody ConsumptionDTO request) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ConsumptionDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ConsumptionDTO.ConsumptionResponse> response =
                restTemplate.postForEntity(
                        pythonServiceUrl,
                        entity,
                        ConsumptionDTO.ConsumptionResponse.class
                );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return ResponseEntity.ok(response.getBody());
        }

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ConsumptionDTO.ConsumptionResponse.builder()
                        .answer("연결 안돼용")
                        .build());
    }
}
