package com.coopcredit.infrastructure.adapter.out.persistence;

import com.coopcredit.domain.model.Affiliate;
import com.coopcredit.domain.port.out.AffiliateRepositoryPort;
import com.coopcredit.infrastructure.adapter.out.persistence.entity.AffiliateEntity;
import com.coopcredit.infrastructure.adapter.out.persistence.repository.SpringDataAffiliateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AffiliateRepositoryAdapter implements AffiliateRepositoryPort {

    private final SpringDataAffiliateRepository repository;

    @Override
    public Affiliate save(Affiliate affiliate) {
        AffiliateEntity entity = toEntity(affiliate);
        AffiliateEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Affiliate> findByDocument(String document) {
        return repository.findByDocument(document).map(this::toDomain);
    }

    @Override
    public java.util.List<Affiliate> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    private AffiliateEntity toEntity(Affiliate domain) {
        return AffiliateEntity.builder()
                .id(domain.getId())
                .document(domain.getDocument())
                .name(domain.getName())
                .salary(domain.getSalary())
                .affiliationDate(domain.getAffiliationDate())
                .status(domain.getStatus().name())
                .build();
    }

    private Affiliate toDomain(AffiliateEntity entity) {
        return Affiliate.builder()
                .id(entity.getId())
                .document(entity.getDocument())
                .name(entity.getName())
                .salary(entity.getSalary())
                .affiliationDate(entity.getAffiliationDate())
                .status(Affiliate.AffiliateStatus.valueOf(entity.getStatus()))
                .build();
    }
}
