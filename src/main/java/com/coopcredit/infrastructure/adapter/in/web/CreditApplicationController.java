package com.coopcredit.infrastructure.adapter.in.web;

import com.coopcredit.domain.model.CreditApplication;
import com.coopcredit.domain.port.in.ProcessCreditApplicationUseCase;
import com.coopcredit.infrastructure.adapter.in.web.dto.CreateCreditApplicationRequest;
import com.coopcredit.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.coopcredit.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class CreditApplicationController {

    private final ProcessCreditApplicationUseCase processCreditApplicationUseCase;
    private final SpringDataUserRepository userRepository;

    @PostMapping
    public ResponseEntity<CreditApplication> createApplication(
            @Valid @RequestBody CreateCreditApplicationRequest request) {

        // Get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Get affiliate document - either from user's linked affiliate or from request
        String affiliateDocument = resolveAffiliateDocument(username, request.getAffiliateDocument());

        CreditApplication application = CreditApplication.builder()
                .requestedAmount(request.getAmount())
                .termMonths(request.getTerm())
                .build();

        CreditApplication result = processCreditApplicationUseCase.registerApplication(application, affiliateDocument);

        return ResponseEntity.created(URI.create("/api/solicitudes/" + result.getId()))
                .body(result);
    }

    /**
     * For ROLE_AFILIADO: Uses the user's linked affiliate document (ignores request
     * document)
     * For ROLE_ADMIN/ROLE_ANALISTA: Uses the document from request (for creating on
     * behalf of)
     */
    private String resolveAffiliateDocument(String username, String requestDocument) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // If user has a linked affiliate, use that document
        if (user.getAffiliate() != null) {
            return user.getAffiliate().getDocument();
        }

        // If user is AFILIADO but not linked, reject
        if ("ROLE_AFILIADO".equals(user.getRole())) {
            throw new IllegalArgumentException(
                    "Your account is not linked to an affiliate. Please contact the administrator.");
        }

        // ADMIN or ANALISTA can specify document
        if (requestDocument == null || requestDocument.isBlank()) {
            throw new IllegalArgumentException("You must specify the affiliate document");
        }

        return requestDocument;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditApplication> getApplication(@PathVariable Long id) {
        return processCreditApplicationUseCase.getApplication(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public java.util.List<CreditApplication> getAllApplications() {
        return processCreditApplicationUseCase.getAllApplications();
    }

    @PostMapping("/{id}/evaluar")
    public ResponseEntity<CreditApplication> evaluateApplication(@PathVariable Long id) {
        CreditApplication evaluated = processCreditApplicationUseCase.evaluateApplication(id);
        return ResponseEntity.ok(evaluated);
    }

    @GetMapping("/pendientes")
    public ResponseEntity<java.util.List<CreditApplication>> getPendingApplications() {
        java.util.List<CreditApplication> pending = processCreditApplicationUseCase
                .getApplicationsByStatus(CreditApplication.CreditStatus.PENDING);
        return ResponseEntity.ok(pending);
    }
}
