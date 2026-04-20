package com.email_service.enums;

/**
 * Represents the delivery status of a sent email.
 * Persisted as a STRING in the email_logs table.
 */
public enum EmailStatus {

    /** Email was successfully delivered to the SMTP server. */
    SENT,

    /** Email failed to send after all retry attempts were exhausted. */
    FAILED
}
