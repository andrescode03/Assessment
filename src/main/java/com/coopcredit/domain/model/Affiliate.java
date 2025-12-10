package com.coopcredit.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Affiliate {
    private Long id;
    private String document;
    private String name;
    private BigDecimal salary;
    private LocalDate affiliationDate;
    private AffiliateStatus status;

    public enum AffiliateStatus {
        ACTIVE, INACTIVE
    }

    public boolean isActive() {
        return this.status == AffiliateStatus.ACTIVE;
    }
}
