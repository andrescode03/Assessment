package com.coopcredit.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_applications")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreditApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affiliate_id", nullable = false)
    private AffiliateEntity affiliate;

    @Column(name = "requested_amount", nullable = false)
    private BigDecimal requestedAmount;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    @Column(name = "proposed_rate")
    private BigDecimal proposedRate;

    @Column(name = "application_date", nullable = false)
    private LocalDateTime applicationDate;

    @Column(nullable = false)
    private String status;

    // Risk Evaluation Embedded or Flat
    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "risk_level")
    private String riskLevel;

    @Column(name = "decision_reason")
    private String decisionReason;

    @Column(name = "evaluation_date")
    private LocalDateTime evaluationDate;
}
