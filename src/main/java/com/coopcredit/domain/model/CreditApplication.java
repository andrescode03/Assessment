package com.coopcredit.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreditApplication {
    private Long id;
    private Affiliate affiliate;
    private BigDecimal requestedAmount;
    private Integer termMonths;
    private BigDecimal proposedRate;
    private LocalDateTime applicationDate;
    private CreditStatus status;
    private RiskEvaluation riskEvaluation;

    public enum CreditStatus {
        PENDING, APPROVED, REJECTED
    }
}
