package com.coopcredit;

import com.coopcredit.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserListTest {

    @Autowired
    private SpringDataUserRepository userRepository;

    @Test
    void listAllUsers() {
        System.out.println("=== USERS IN DATABASE ===");
        userRepository.findAll()
                .forEach(u -> System.out.println("User: " + u.getUsername() + " | Role: " + u.getRole()));
        System.out.println("=========================");
    }
}
