package com.coopcredit.infrastructure.adapter.out.persistence.repository;

import com.coopcredit.infrastructure.adapter.out.persistence.entity.AffiliateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataAffiliateRepository extends JpaRepository<AffiliateEntity, Long> {
    Optional<AffiliateEntity> findByDocument(String document);
}
