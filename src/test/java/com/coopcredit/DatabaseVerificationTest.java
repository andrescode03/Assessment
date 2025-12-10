package com.coopcredit;

import com.coopcredit.infrastructure.adapter.out.persistence.repository.SpringDataAffiliateRepository;
import com.coopcredit.infrastructure.adapter.out.persistence.repository.SpringDataCreditApplicationRepository;
import com.coopcredit.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DatabaseVerificationTest {

    @Autowired
    private SpringDataUserRepository userRepository;

    @Autowired
    private SpringDataAffiliateRepository affiliateRepository;

    @Autowired
    private SpringDataCreditApplicationRepository creditRepository;

    @Test
    void printDatabaseStats() {
        System.out.println("=== DATABASE VERIFICATION ===");
        System.out.println("Users Count: " + userRepository.count());
        System.out.println("Affiliates Count: " + affiliateRepository.count());
        System.out.println("Credit Applications Count: " + creditRepository.count());

        System.out.println("\n--- Latest Affiliates ---");
        affiliateRepository.findAll()
                .forEach(a -> System.out.println("Affiliate: " + a.getDocument() + " - " + a.getName()));
        System.out.println("=============================");
    }
}
