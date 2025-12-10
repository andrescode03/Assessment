package com.coopcredit.infrastructure.adapter.in.web;

import com.coopcredit.infrastructure.adapter.out.persistence.entity.AffiliateEntity;
import com.coopcredit.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.coopcredit.infrastructure.adapter.out.persistence.repository.SpringDataAffiliateRepository;
import com.coopcredit.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin-only controller for managing users and linking them to affiliates
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final SpringDataUserRepository userRepository;
    private final SpringDataAffiliateRepository affiliateRepository;

    /**
     * List all users with their affiliate links
     */
    @GetMapping
    public List<UserSummary> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    /**
     * Link a user to an affiliate
     * Only ADMIN can perform this action
     */
    @PostMapping("/{userId}/link-affiliate")
    public ResponseEntity<?> linkUserToAffiliate(
            @PathVariable Long userId,
            @RequestBody LinkAffiliateRequest request) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        AffiliateEntity affiliate = affiliateRepository.findByDocument(request.getAffiliateDocument())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Afiliado no encontrado con documento: " + request.getAffiliateDocument()));

        user.setAffiliate(affiliate);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Usuario vinculado exitosamente al afiliado",
                "username", user.getUsername(),
                "affiliateDocument", affiliate.getDocument(),
                "affiliateName", affiliate.getName()));
    }

    /**
     * Unlink a user from their affiliate
     */
    @DeleteMapping("/{userId}/unlink-affiliate")
    public ResponseEntity<?> unlinkUserFromAffiliate(@PathVariable Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        user.setAffiliate(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Usuario desvinculado del afiliado"));
    }

    private UserSummary toSummary(UserEntity user) {
        UserSummary summary = new UserSummary();
        summary.setId(user.getId());
        summary.setUsername(user.getUsername());
        summary.setRole(user.getRole());
        if (user.getAffiliate() != null) {
            summary.setAffiliateDocument(user.getAffiliate().getDocument());
            summary.setAffiliateName(user.getAffiliate().getName());
        }
        return summary;
    }

    @Data
    public static class LinkAffiliateRequest {
        private String affiliateDocument;
    }

    @Data
    public static class UserSummary {
        private Long id;
        private String username;
        private String role;
        private String affiliateDocument;
        private String affiliateName;
    }
}
