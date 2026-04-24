package com.email_service.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestDTO {

	/**
	 * Identifies which sender account to use for this email. Must match an
	 * {@code accountKey} in the {@code email_accounts} table.
	 */
	private String accountKey;

	/*
	 * Comma-separated list of recipient email addresses. For multiple recipients, use a comma to separate them, e.g.:
	 */
    private String to;

    /*
     * Comma-separated list of CC email addresses. Optional. For multiple CC recipients, use a comma to separate them, e.g.:
     */
    private String cc;

    private String subject;

    /**
     * This field does NOT select or trigger any template inside the email-service.
     * {@code "PASSWORD_RESET"}, {@code "OTP"}, {@code "ORDER_CONFIRMATION"}, etc.
     */
    private String templateName;

    /**
     * The complete HTML content of the email body
     * If inline attachments are used for images, reference them via CID:
     * 
     * <pre>{@code
     * <img src="cid:logo" alt="Brand Logo" />
     * }</pre>
     * 
     * If images are externally hosted, use a regular URL instead.
     */
    private String body;

    /**
     * Optional list of inline attachments to embed inside the email body.
     * 
     * Each {@link EmailAttachmentDTO} must have a {@code contentId} that matches
     * the {@code cid:} reference used in the HTML {@code body} field.
     */
    private List<EmailAttachmentDTO> inlineAttachments;
}
