package com.coopcredit.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // ROLE_ADMIN, ROLE_AFILIADO, ROLE_ANALISTA

    // Link to Affiliate (nullable - only for ROLE_AFILIADO users)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "affiliate_id")
    private AffiliateEntity affiliate;
}
