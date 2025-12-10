package com.coopcredit.infrastructure.adapter.out.external;

import com.coopcredit.domain.model.RiskEvaluation;
import com.coopcredit.domain.port.out.RiskServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RiskServiceAdapter implements RiskServicePort {

    private final RestTemplate restTemplate;

    @org.springframework.beans.factory.annotation.Value("${risk.service.url:http://localhost:8082/risk-evaluation}")
    private String riskServiceUrl;

    @Override
    public RiskEvaluation evaluateRisk(String document, BigDecimal amount, Integer termMonths) {
        Map<String, Object> request = new HashMap<>();
        request.put("documento", document);
        request.put("monto", amount);
        request.put("plazo", termMonths);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(riskServiceUrl, request, Map.class);

            if (response == null) {
                throw new RuntimeException("Error calling risk service: Empty response");
            }

            return RiskEvaluation.builder()
                    .score((Integer) response.get("score"))
                    .riskLevel((String) response.get("nivelRiesgo"))
                    .decisionReason((String) response.get("detalle"))
                    .evaluationDate(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate risk: " + e.getMessage(), e);
        }
    }
}
