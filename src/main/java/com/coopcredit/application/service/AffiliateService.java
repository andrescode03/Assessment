package com.coopcredit.application.service;

import com.coopcredit.domain.model.Affiliate;
import com.coopcredit.domain.port.in.ManageAffiliateUseCase;
import com.coopcredit.domain.port.out.AffiliateRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AffiliateService implements ManageAffiliateUseCase {

    private final AffiliateRepositoryPort affiliateRepository;

    @Override
    @Transactional
    public Affiliate createAffiliate(Affiliate affiliate) {
        if (affiliateRepository.findByDocument(affiliate.getDocument()).isPresent()) {
            throw new IllegalArgumentException("Affiliate with this document already exists");
        }
        // Default Active
        affiliate.setStatus(Affiliate.AffiliateStatus.ACTIVE);
        return affiliateRepository.save(affiliate);
    }

    @Override
    public Optional<Affiliate> getAffiliate(String document) {
        return affiliateRepository.findByDocument(document);
    }

    @Override
    @Transactional
    public Affiliate updateAffiliate(String document, Affiliate affiliate) {
        Affiliate existing = affiliateRepository.findByDocument(document)
                .orElseThrow(() -> new IllegalArgumentException("Afiliado no encontrado: " + document));

        // Update Allowed Fields (only if provided)
        if (affiliate.getName() != null) {
            existing.setName(affiliate.getName());
        }
        if (affiliate.getSalary() != null) {
            existing.setSalary(affiliate.getSalary());
        }
        if (affiliate.getAffiliationDate() != null) {
            existing.setAffiliationDate(affiliate.getAffiliationDate());
        }
        if (affiliate.getStatus() != null) {
            existing.setStatus(affiliate.getStatus());
        }

        return affiliateRepository.save(existing);
    }

    @Override
    public java.util.List<Affiliate> getAllAffiliates() {
        return affiliateRepository.findAll();
    }
}
