package com.coopcredit.infrastructure.adapter.in.web;

import com.coopcredit.infrastructure.adapter.out.persistence.entity.AffiliateEntity;
import com.coopcredit.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.coopcredit.infrastructure.adapter.out.persistence.repository.SpringDataAffiliateRepository;
import com.coopcredit.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import com.coopcredit.infrastructure.configuration.security.JwtService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SpringDataUserRepository userRepository;
    private final SpringDataAffiliateRepository affiliateRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        String role = request.getRole() != null ? request.getRole() : "ROLE_AFILIADO";

        // If role is AFILIADO, try to link with an existing affiliate
        AffiliateEntity linkedAffiliate = null;
        if ("ROLE_AFILIADO".equals(role)) {
            // Search affiliate by document = username (or use specific field affiliateDocument)
            String affiliateDoc = request.getAffiliateDocument() != null
                    ? request.getAffiliateDocument()
                    : request.getUsername();

            linkedAffiliate = affiliateRepository.findByDocument(affiliateDoc)
                    .orElse(null);

            if (linkedAffiliate == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error",
                                "No affiliate found with document: " + affiliateDoc +
                                        ". You must create the affiliate before registering the user."));
            }
        }

        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .affiliate(linkedAffiliate)
                .build();

        userRepository.save(user);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("message", "User registered successfully");
        response.put("role", role);
        if (linkedAffiliate != null) {
            response.put("linkedAffiliate", linkedAffiliate.getDocument());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));
        final UserDetails user = userDetailsService.loadUserByUsername(request.getUsername());
        final String jwt = jwtService.generateToken(user);
        return ResponseEntity.ok(Map.of("token", jwt));
    }

    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
        private String role;
        private String affiliateDocument; // Optional: affiliate document to link
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
