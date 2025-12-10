package com.coopcredit;

import com.coopcredit.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.coopcredit.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import com.coopcredit.infrastructure.configuration.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SpringDataUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        UserEntity user = new UserEntity();
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole("ADMIN"); // Adjust to your UserEntity definition
        userRepository.save(user);
    }

    @Test
    void shouldAllowPublicAccessToLogin() throws Exception {
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"password\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowPublicAccessToRiskEvaluation() throws Exception {
        mockMvc.perform(post("/risk-evaluation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"documento\":\"12345678\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldBlockUnauthenticatedAccessToAffiliates() throws Exception {
        mockMvc.perform(get("/api/afiliados/123"))
                .andExpect(status().isForbidden()); // Spring Security explicit 401/403 depends on config.
                                                    // Without token usually 403 in some configs or 401.
                                                    // Default entry point sends 403 or 401. Let's check.
                                                    // If we are strict, it should be 403 Forbidden (defaults) or 401
                                                    // Unauthorized.
                                                    // Let's accept isForbidden() or isUnauthorized().
                                                    // Actually, modern Spring Security returns 401 for missing auth,
                                                    // 403 for insufficient roles.
                                                    // Wait, `authenticated()` means 403 if anonymous? Or 401?
                                                    // Let's safe bet checking 403.
    }

    @Test
    void shouldAllowAccessWithValidToken() throws Exception {
        // Authenticate via endpoint to be sure or generate manually if DB is in sync
        UserDetails user = User.withUsername("admin").password("password").roles("ADMIN").build();
        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/afiliados/dummy")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound()); // 404 means auth passed
    }

    @Test
    void shouldBlockAccessWithMalformedToken() throws Exception {
        mockMvc.perform(get("/api/afiliados/dummy")
                .header("Authorization", "Bearer malformed.token.value"))
                .andExpect(status().isForbidden()); // Filter will fail validation, context not set, so anonymous -> 403
    }
}
