package com.coopcredit.infrastructure.adapter.in.web;

import com.coopcredit.domain.model.Affiliate;
import com.coopcredit.domain.port.in.ManageAffiliateUseCase;
import com.coopcredit.infrastructure.adapter.in.web.dto.CreateAffiliateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/afiliados")
@RequiredArgsConstructor
public class AffiliateController {

    private final ManageAffiliateUseCase manageAffiliateUseCase;

    @PostMapping
    public ResponseEntity<Affiliate> createAffiliate(@Valid @RequestBody CreateAffiliateRequest request) {
        Affiliate affiliate = Affiliate.builder()
                .document(request.getDocument())
                .name(request.getName())
                .salary(request.getSalary())
                .affiliationDate(request.getAffiliationDate())
                .build();
        Affiliate created = manageAffiliateUseCase.createAffiliate(affiliate);
        return ResponseEntity.created(URI.create("/api/afiliados/" + created.getDocument()))
                .body(created);
    }

    @GetMapping("/{document}")
    public ResponseEntity<Affiliate> getAffiliate(@PathVariable String document) {
        return manageAffiliateUseCase.getAffiliate(document)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{document}")
    public ResponseEntity<?> updateAffiliate(@PathVariable String document,
            @Valid @RequestBody com.coopcredit.infrastructure.adapter.in.web.dto.UpdateAffiliateRequest request) {
        try {
            Affiliate affiliate = Affiliate.builder()
                    .name(request.getName())
                    .salary(request.getSalary())
                    .status(Affiliate.AffiliateStatus.valueOf(request.getStatus()))
                    .build();
            Affiliate updated = manageAffiliateUseCase.updateAffiliate(document, affiliate);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public java.util.List<Affiliate> getAllAffiliates() {
        return manageAffiliateUseCase.getAllAffiliates();
    }
}
