package com.coopcredit.application.service;

import com.coopcredit.domain.model.Affiliate;
import com.coopcredit.domain.model.CreditApplication;
import com.coopcredit.domain.model.RiskEvaluation;
import com.coopcredit.domain.port.in.ProcessCreditApplicationUseCase;
import com.coopcredit.domain.port.out.AffiliateRepositoryPort;
import com.coopcredit.domain.port.out.CreditApplicationRepositoryPort;
import com.coopcredit.domain.port.out.RiskServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreditApplicationService implements ProcessCreditApplicationUseCase {

    private final CreditApplicationRepositoryPort creditRepository;
    private final AffiliateRepositoryPort affiliateRepository;
    private final RiskServicePort riskService;

    @Override
    @Transactional
    public CreditApplication registerApplication(CreditApplication application, String affiliateDocument) {
        Affiliate affiliate = affiliateRepository.findByDocument(affiliateDocument)
                .orElseThrow(() -> new IllegalArgumentException("Affiliate not found"));

        if (!affiliate.isActive()) {
            throw new IllegalArgumentException("Affiliate is not ACTIVE");
        }

        // 1. Validate Seniority (Minimum 6 months)
        if (affiliate.getAffiliationDate().isAfter(java.time.LocalDate.now().minusMonths(6))) {
            throw new IllegalArgumentException("Affiliate seniority must be at least 6 months");
        }

        // 2. Validate Debt Capacity (Monthly installment <= 50% of salary)
        java.math.BigDecimal installment = application.getRequestedAmount()
                .divide(java.math.BigDecimal.valueOf(application.getTermMonths()), java.math.RoundingMode.CEILING);

        java.math.BigDecimal maxCapacity = affiliate.getSalary().multiply(java.math.BigDecimal.valueOf(0.5));

        if (installment.compareTo(maxCapacity) > 0) {
            throw new IllegalArgumentException(
                    String.format("Estimated monthly installment ($%s) exceeds 50%% of salary", installment));
        }

        // 3. Validate Maximum Amount (max 12x salary)
        java.math.BigDecimal maxAmount = affiliate.getSalary().multiply(java.math.BigDecimal.valueOf(12));
        if (application.getRequestedAmount().compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException(
                    String.format("Requested amount ($%s) exceeds maximum allowed ($%s = 12x salary)",
                            application.getRequestedAmount(), maxAmount));
        }

        application.setAffiliate(affiliate);
        application.setApplicationDate(LocalDateTime.now());
        application.setStatus(CreditApplication.CreditStatus.PENDING);

        // Save with PENDING status - Risk evaluation will be done separately
        return creditRepository.save(application);
    }

    @Override
    @Transactional
    public CreditApplication evaluateApplication(Long applicationId) {
        CreditApplication application = creditRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (application.getStatus() != CreditApplication.CreditStatus.PENDING) {
            throw new IllegalArgumentException(
                    String.format("Application has already been evaluated. Current status: %s", application.getStatus()));
        }

        // Call Risk Service with document, amount, term
        RiskEvaluation risk = riskService.evaluateRisk(
                application.getAffiliate().getDocument(),
                application.getRequestedAmount(),
                application.getTermMonths());
        application.setRiskEvaluation(risk);

        // Business Logic for Approval/Rejection
        if ("ALTO".equals(risk.getRiskLevel())) {
            application.setStatus(CreditApplication.CreditStatus.REJECTED);
        } else {
            application.setStatus(CreditApplication.CreditStatus.APPROVED);
        }

        return creditRepository.save(application);
    }

    @Override
    public java.util.Optional<CreditApplication> getApplication(Long id) {
        return creditRepository.findById(id);
    }

    @Override
    public java.util.List<CreditApplication> getAllApplications() {
        // Warning: This could be large in production. Pagination recommended for
        // future.
        // Since findAll() is not in the Port by default, we need to add it or use a
        // specific finder.
        // For strict Hexagonal, we should add findAll to
        // CreditApplicationRepositoryPort.
        return creditRepository.findAll();
    }

    @Override
    public java.util.List<CreditApplication> getApplicationsByStatus(CreditApplication.CreditStatus status) {
        return creditRepository.findByStatus(status);
    }

    @Override
    public java.util.List<CreditApplication> getApplicationsByAffiliateDocument(String document) {
        return creditRepository.findByAffiliateDocument(document);
    }

}

