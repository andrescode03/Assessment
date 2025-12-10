package com.coopcredit.application.service;

import com.coopcredit.domain.model.Affiliate;
import com.coopcredit.domain.model.CreditApplication;
import com.coopcredit.domain.model.RiskEvaluation;
import com.coopcredit.domain.port.out.AffiliateRepositoryPort;
import com.coopcredit.domain.port.out.CreditApplicationRepositoryPort;
import com.coopcredit.domain.port.out.RiskServicePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditApplicationServiceTest {

    @Mock
    private CreditApplicationRepositoryPort creditRepository;
    @Mock
    private AffiliateRepositoryPort affiliateRepository;
    @Mock
    private RiskServicePort riskService;

    @InjectMocks
    private CreditApplicationService service;

    @Test
    void shouldApproveApplication_WhenRulesPass() {
        // Arrange
        String doc = "12345";
        Affiliate affiliate = Affiliate.builder()
                .document(doc)
                .salary(new BigDecimal("1000"))
                .affiliationDate(LocalDate.now().minusMonths(7)) // > 6 months
                .status(Affiliate.AffiliateStatus.ACTIVE)
                .build();

        // Amount 100, Term 10 => Installment 10. Max Capacity 500 (50% of 1000). 10 <=
        // 500 OK.
        CreditApplication application = CreditApplication.builder()
                .requestedAmount(new BigDecimal("100"))
                .termMonths(10)
                .build();

        RiskEvaluation lowRisk = RiskEvaluation.builder().riskLevel("BAJO").build();

        when(affiliateRepository.findByDocument(doc)).thenReturn(Optional.of(affiliate));
        when(riskService.evaluateRisk(any(), any(), any())).thenReturn(lowRisk);
        when(creditRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        CreditApplication result = service.registerApplication(application, doc);

        // Assert
        assertEquals(CreditApplication.CreditStatus.APPROVED, result.getStatus());
    }

    @Test
    void shouldRejectApplication_WhenRiskIsHigh() {
        // Arrange
        String doc = "12345";
        Affiliate affiliate = Affiliate.builder()
                .document(doc)
                .salary(new BigDecimal("1000"))
                .affiliationDate(LocalDate.now().minusMonths(7))
                .status(Affiliate.AffiliateStatus.ACTIVE)
                .build();

        CreditApplication application = CreditApplication.builder()
                .requestedAmount(new BigDecimal("100"))
                .termMonths(10)
                .build();

        RiskEvaluation highRisk = RiskEvaluation.builder().riskLevel("ALTO").build();

        when(affiliateRepository.findByDocument(doc)).thenReturn(Optional.of(affiliate));
        when(riskService.evaluateRisk(any(), any(), any())).thenReturn(highRisk);
        when(creditRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        CreditApplication result = service.registerApplication(application, doc);

        // Assert
        assertEquals(CreditApplication.CreditStatus.REJECTED, result.getStatus());
    }

    @Test
    void shouldThrow_WhenAffiliateNotFound() {
        when(affiliateRepository.findByDocument("999")).thenReturn(Optional.empty());
        CreditApplication app = CreditApplication.builder().build();

        assertThrows(IllegalArgumentException.class, () -> service.registerApplication(app, "999"));
    }

    @Test
    void shouldThrow_WhenAffiliateInactive() {
        Affiliate affiliate = Affiliate.builder().status(Affiliate.AffiliateStatus.INACTIVE).build();
        when(affiliateRepository.findByDocument("123")).thenReturn(Optional.of(affiliate));
        CreditApplication app = CreditApplication.builder().build();

        assertThrows(IllegalArgumentException.class, () -> service.registerApplication(app, "123"));
    }

    @Test
    void shouldThrow_WhenSeniorityTooLow() {
        Affiliate affiliate = Affiliate.builder()
                .status(Affiliate.AffiliateStatus.ACTIVE)
                .affiliationDate(LocalDate.now().minusMonths(5)) // < 6 months
                .build();
        when(affiliateRepository.findByDocument("123")).thenReturn(Optional.of(affiliate));
        CreditApplication app = CreditApplication.builder().build();

        Exception ex = assertThrows(IllegalArgumentException.class, () -> service.registerApplication(app, "123"));
        assertTrue(ex.getMessage().contains("antigüedad"));
    }

    @Test
    void shouldThrow_WhenCapacityExceeded() {
        // Salary 1000. Max Installment 500.
        // Request 600 in 1 month => Installment 600. 600 > 500.
        Affiliate affiliate = Affiliate.builder()
                .document("123")
                .status(Affiliate.AffiliateStatus.ACTIVE)
                .affiliationDate(LocalDate.now().minusMonths(7))
                .salary(new BigDecimal("1000"))
                .build();

        CreditApplication app = CreditApplication.builder()
                .requestedAmount(new BigDecimal("600"))
                .termMonths(1)
                .build();

        when(affiliateRepository.findByDocument("123")).thenReturn(Optional.of(affiliate));

        Exception ex = assertThrows(IllegalArgumentException.class, () -> service.registerApplication(app, "123"));
        assertTrue(ex.getMessage().contains("excede el 50%"));
    }
}
