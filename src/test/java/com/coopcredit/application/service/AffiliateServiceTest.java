package com.coopcredit.application.service;

import com.coopcredit.domain.model.Affiliate;
import com.coopcredit.domain.port.out.AffiliateRepositoryPort;
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
class AffiliateServiceTest {

    @Mock
    private AffiliateRepositoryPort affiliateRepository;

    @InjectMocks
    private AffiliateService affiliateService;

    @Test
    void createAffiliate_WhenNew_ShouldSave() {
        Affiliate affiliate = Affiliate.builder()
                .document("12345678")
                .name("John Doe")
                .salary(new BigDecimal("1000"))
                .affiliationDate(LocalDate.now())
                .build();

        when(affiliateRepository.findByDocument("12345678")).thenReturn(Optional.empty());
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(affiliate);

        Affiliate result = affiliateService.createAffiliate(affiliate);

        assertNotNull(result);
        assertEquals(Affiliate.AffiliateStatus.ACTIVE, result.getStatus());
        verify(affiliateRepository).save(affiliate);
    }

    @Test
    void createAffiliate_WhenExists_ShouldThrowException() {
        Affiliate affiliate = Affiliate.builder().document("12345678").build();

        when(affiliateRepository.findByDocument("12345678")).thenReturn(Optional.of(affiliate));

        assertThrows(IllegalArgumentException.class, () -> affiliateService.createAffiliate(affiliate));
        verify(affiliateRepository, never()).save(any());
    }

    @Test
    void getAffiliate_WhenExists_ShouldReturn() {
        Affiliate affiliate = Affiliate.builder().document("12345678").build();
        when(affiliateRepository.findByDocument("12345678")).thenReturn(Optional.of(affiliate));

        Optional<Affiliate> result = affiliateService.getAffiliate("12345678");

        assertTrue(result.isPresent());
        assertEquals("12345678", result.get().getDocument());
    }

    @Test
    void updateAffiliate_WhenExists_ShouldUpdate() {
        Affiliate existing = Affiliate.builder()
                .document("12345678")
                .name("Old Name")
                .salary(BigDecimal.TEN)
                .build();

        Affiliate updateData = Affiliate.builder()
                .name("New Name")
                .salary(BigDecimal.ONE)
                .affiliationDate(LocalDate.now())
                .status(Affiliate.AffiliateStatus.INACTIVE)
                .build();

        when(affiliateRepository.findByDocument("12345678")).thenReturn(Optional.of(existing));
        when(affiliateRepository.save(any(Affiliate.class))).thenAnswer(i -> i.getArguments()[0]);

        Affiliate result = affiliateService.updateAffiliate("12345678", updateData);

        assertEquals("New Name", result.getName());
        assertEquals(Affiliate.AffiliateStatus.INACTIVE, result.getStatus());
    }

    @Test
    void updateAffiliate_WhenNotExists_ShouldThrowException() {
        Affiliate updateData = Affiliate.builder().build();
        when(affiliateRepository.findByDocument("12345678")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> affiliateService.updateAffiliate("12345678", updateData));
    }
}
