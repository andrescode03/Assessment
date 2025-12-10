package com.coopcredit.domain.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RiskEvaluation {
    private Integer score;
    private String riskLevel;
    private String decisionReason;
    private LocalDateTime evaluationDate;
}
