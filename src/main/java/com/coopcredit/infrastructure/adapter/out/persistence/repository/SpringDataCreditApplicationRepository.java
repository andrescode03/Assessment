package com.coopcredit.infrastructure.adapter.out.persistence.repository;

import com.coopcredit.infrastructure.adapter.out.persistence.entity.CreditApplicationEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.NonNull;

public interface SpringDataCreditApplicationRepository extends JpaRepository<CreditApplicationEntity, Long> {

    @Override
    @EntityGraph(attributePaths = "affiliate")
    @NonNull
    List<CreditApplicationEntity> findAll();

    @Override
    @EntityGraph(attributePaths = "affiliate")
    @NonNull
    Optional<CreditApplicationEntity> findById(@NonNull Long id);

    @EntityGraph(attributePaths = "affiliate")
    List<CreditApplicationEntity> findByStatus(String status);

    @EntityGraph(attributePaths = "affiliate")
    List<CreditApplicationEntity> findByAffiliateDocument(String document);
}

