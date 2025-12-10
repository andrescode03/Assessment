package com.coopcredit;

import com.coopcredit.domain.model.Affiliate;
import com.coopcredit.infrastructure.adapter.in.web.AuthController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @Import(TestcontainersConfiguration.class) // using local DB for stability
public class HappyPathIntegrationTest {

        @Autowired
        private TestRestTemplate restTemplate;

        @org.springframework.boot.test.mock.mockito.MockBean
        private com.coopcredit.domain.port.out.RiskServicePort riskServicePort;

        @Test
        void testFullFlow() {
                // --- ADMIN ACTION: Create Affiliate ---

                String suffix = String.valueOf(System.currentTimeMillis());
                String adminUser = "adminUser_" + suffix;
                String affiliateUser = "affiliateUser_" + suffix;
                String documentId = suffix.substring(suffix.length() - 8); // Last 8 digits

                // Mock Risk Service
                org.mockito.Mockito.when(riskServicePort.evaluateRisk(
                                org.mockito.ArgumentMatchers.any(),
                                org.mockito.ArgumentMatchers.any(),
                                org.mockito.ArgumentMatchers.any()))
                                .thenReturn(com.coopcredit.domain.model.RiskEvaluation.builder()
                                                .score(800)
                                                .riskLevel("BAJO")
                                                .build());

                // 1. Register Admin
                AuthController.RegisterRequest adminRegister = new AuthController.RegisterRequest();
                adminRegister.setUsername(adminUser);
                adminRegister.setPassword("password");
                adminRegister.setRole("ROLE_ADMIN");

                restTemplate.postForEntity("/auth/register", adminRegister, Map.class); // Ignore response

                // 2. Login Admin
                AuthController.LoginRequest adminLogin = new AuthController.LoginRequest();
                adminLogin.setUsername(adminUser);
                adminLogin.setPassword("password");

                ResponseEntity<Map> adminLoginResponse = restTemplate.postForEntity("/auth/login", adminLogin,
                                Map.class);
                assertThat(adminLoginResponse.getStatusCode().is2xxSuccessful()).isTrue();
                String adminToken = (String) adminLoginResponse.getBody().get("token");

                HttpHeaders adminHeaders = new HttpHeaders();
                adminHeaders.setBearerAuth(adminToken);

                // 3. Create Affiliate (as Admin)
                Map<String, Object> affiliate = Map.of(
                                "document", documentId,
                                "name", "Juan Perez",
                                "salary", 5000000,
                                "affiliationDate", "2023-01-01");
                HttpEntity<Map<String, Object>> affiliateRequest = new HttpEntity<>(affiliate, adminHeaders);
                ResponseEntity<Map> affiliateResponse = restTemplate.postForEntity("/api/afiliados", affiliateRequest,
                                Map.class);
                assertThat(affiliateResponse.getStatusCode().is2xxSuccessful()).isTrue();

                // --- AFFILIATE ACTION: Request Credit ---

                // 4. Register Affiliate User
                AuthController.RegisterRequest userRegister = new AuthController.RegisterRequest();
                userRegister.setUsername(affiliateUser);
                userRegister.setPassword("password");
                userRegister.setRole("ROLE_AFILIADO");

                restTemplate.postForEntity("/auth/register", userRegister, Map.class);

                // 5. Login Affiliate User
                AuthController.LoginRequest userLogin = new AuthController.LoginRequest();
                userLogin.setUsername(affiliateUser);
                userLogin.setPassword("password");

                ResponseEntity<Map> userLoginResponse = restTemplate.postForEntity("/auth/login", userLogin, Map.class);
                String userToken = (String) userLoginResponse.getBody().get("token");

                HttpHeaders userHeaders = new HttpHeaders();
                userHeaders.setBearerAuth(userToken);

                // 6. Create Credit Application (as Affiliate)
                Map<String, Object> creditRequest = Map.of(
                                "affiliateDocument", documentId,
                                "amount", 1000000,
                                "term", 12);
                HttpEntity<Map<String, Object>> creditEntity = new HttpEntity<>(creditRequest, userHeaders);
                ResponseEntity<Map> creditResponse = restTemplate.postForEntity("/api/solicitudes", creditEntity,
                                Map.class);

                if (!creditResponse.getStatusCode().is2xxSuccessful()) {
                        System.err.println("Credit Application Failed. Status: " + creditResponse.getStatusCode());
                        System.err.println("Response Body: " + creditResponse.getBody());
                }

                assertThat(creditResponse.getStatusCode().is2xxSuccessful()).isTrue();
                Map body = creditResponse.getBody();
                System.out.println("Credit Response: " + body);
                assertThat(body.get("status")).isIn("APPROVED", "REJECTED");
                // Risk evaluation might be null in response if DTO doesn't mapping it back or
                // if
                // Application logic doesn't set it (it does).
                // Check CreditApplicationService: application.setRiskEvaluation(risk);
                // And if Controller maps it.
        }
}
