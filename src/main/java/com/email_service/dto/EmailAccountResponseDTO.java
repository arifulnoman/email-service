package com.email_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API response DTO for {@link com.email_service.entity.EmailAccount}.
 * Password is intentionally excluded.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAccountResponseDTO {

    private Long id;

    private String accountKey;

    private String smtpHost;

    private Integer smtpPort;

    private String username;

    private String fromName;

    private Boolean active;
}
