package com.email_service.entity;

import java.time.LocalDateTime;

import com.email_service.enums.EmailStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Audit log entry for every email processed by the email-service.
 * Mapped to the {@code email_logs} table (auto-created by Hibernate).
 */
@Entity
@Table(name = "email_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The account key used to send this email.
     * Matches {@link com.email_service.entity.EmailAccount#getAccountKey()}.
     */
    @Column(name = "account_key")
    private String accountKey;

    /** Recipient email address. */
    @Column(name = "to_address", nullable = false)
    private String toAddress;

    /** Subject line of the email. */
    @Column(nullable = false)
    private String subject;

    /**
     * Audit label supplied by the producer (e.g. "WELCOME", "OTP").
     */
    @Column(name = "template_name")
    private String templateName;

    /** Whether the email was delivered successfully or failed. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus status;

    /**
     * Error message captured when status is FAILED.
     * Stored as TEXT to accommodate long stack-trace excerpts.
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Full stack trace captured when status is FAILED.
     * Stored as TEXT for complete audit trail of failures.
     */
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    /** Timestamp of the send attempt. */
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}
