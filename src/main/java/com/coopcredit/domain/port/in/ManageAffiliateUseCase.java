package com.coopcredit.domain.port.in;

import com.coopcredit.domain.model.Affiliate;
import java.util.Optional;

public interface ManageAffiliateUseCase {
    Affiliate createAffiliate(Affiliate affiliate);

    Optional<Affiliate> getAffiliate(String document);

    Affiliate updateAffiliate(String document, Affiliate affiliate);

    java.util.List<Affiliate> getAllAffiliates();
}
