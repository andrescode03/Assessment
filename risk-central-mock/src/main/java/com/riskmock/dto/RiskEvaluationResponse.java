package com.riskmock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskEvaluationResponse {
    private String documento;
    private Integer score;
    private String nivelRiesgo;
    private String detalle;
}
