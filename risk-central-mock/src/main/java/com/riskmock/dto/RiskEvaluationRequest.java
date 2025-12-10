package com.riskmock.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RiskEvaluationRequest {
    private String documento;
    private BigDecimal monto;
    private Integer plazo;
}
