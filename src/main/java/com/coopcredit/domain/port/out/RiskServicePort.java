package com.coopcredit.domain.port.out;

import com.coopcredit.domain.model.RiskEvaluation;
import java.math.BigDecimal;

public interface RiskServicePort {
    RiskEvaluation evaluateRisk(String document, BigDecimal amount, Integer termMonths);
}
