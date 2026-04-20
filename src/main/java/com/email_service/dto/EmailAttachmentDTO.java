package com.email_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single inline image or resource to be embedded inside the email.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAttachmentDTO {

    /**
     * The Content-ID (CID) that ties this attachment to the HTML body.
     */
    private String contentId;

    /**
     * The filename of the attachment as it appears in the email.
     */
    private String filename;

    /**
     * The MIME content type of the attachment.
     */
    private String contentType;

    /**
     * The raw byte content of the image or file.
     */
    private byte[] data;
}
