package com.coopcredit.domain.port.out;

import com.coopcredit.domain.model.CreditApplication;
import java.util.List;
import java.util.Optional;

public interface CreditApplicationRepositoryPort {
    CreditApplication save(CreditApplication application);

    Optional<CreditApplication> findById(Long id);

    List<CreditApplication> findAll();

    List<CreditApplication> findByStatus(CreditApplication.CreditStatus status);

    List<CreditApplication> findByAffiliateDocument(String document);
}

