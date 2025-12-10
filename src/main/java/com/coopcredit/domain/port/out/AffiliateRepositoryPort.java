package com.coopcredit.domain.port.out;

import com.coopcredit.domain.model.Affiliate;
import java.util.Optional;

public interface AffiliateRepositoryPort {
    Affiliate save(Affiliate affiliate);

    Optional<Affiliate> findByDocument(String document);

    java.util.List<Affiliate> findAll();
}
