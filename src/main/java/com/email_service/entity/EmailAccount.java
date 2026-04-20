package com.email_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a named SMTP sender account used by the email-service.
 * Stored in the {@code email_accounts} table and cached in Redis.
 */
@Entity
@Table(name = "email_accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique logical key used by producers to select this account.
     * Example: {@code "hrms-client-1"}, {@code "shop-project"}.
     */
    @Column(name = "account_key", nullable = false, unique = true)
    private String accountKey;

    @Column(name = "smtp_host", nullable = false)
    private String smtpHost;

    @Column(name = "smtp_port", nullable = false)
    private Integer smtpPort;

    @Column(nullable = false)
    private String username;

    /** SMTP password or app-specific password. */
    @Column(nullable = false)
    private String password;

    @Column(name = "from_name")
    private String fromName;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
