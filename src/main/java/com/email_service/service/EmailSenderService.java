package com.email_service.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.email_service.dto.EmailAttachmentDTO;
import com.email_service.dto.EmailRequestDTO;
import com.email_service.entity.EmailAccount;
import com.email_service.entity.EmailLog;
import com.email_service.enums.EmailStatus;
import com.email_service.exception.EmailSendException;
import com.email_service.repository.EmailLogRepository;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final EmailAccountService emailAccountService;

    private final MailSenderFactory mailSenderFactory;

    private final EmailLogRepository emailLogRepository;

    public void send(EmailRequestDTO request) {
        EmailAccount account = null;

        try {
            account = emailAccountService.getByAccountKey(request.getAccountKey());
            JavaMailSender mailSender = mailSenderFactory.build(account);

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            boolean hasAttachments = !CollectionUtils.isEmpty(request.getInlineAttachments());

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, hasAttachments, "UTF-8");

            String fromAddress = buildFromAddress(account);
            helper.setFrom(fromAddress);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody(), true);

            if (request.getCc() != null && !request.getCc().isBlank()) {
                helper.setCc(request.getCc());
            }

            if (hasAttachments) {
                for (EmailAttachmentDTO attachment : request.getInlineAttachments()) {
                    helper.addInline(
                            attachment.getContentId(),
                            new ByteArrayResource(attachment.getData()),
                            attachment.getContentType());
                }
            }

            mailSender.send(mimeMessage);

            log.info("Email sent | accountKey={} | to={} | template={}",
                    request.getAccountKey(), request.getTo(), request.getTemplateName());
            persistLog(request, request.getAccountKey(), EmailStatus.SENT, null, null);

        } catch (Exception e) {
            // account may be null if getByAccountKey itself failed (e.g. unknown
            // accountKey)
            String resolvedKey = account != null ? account.getAccountKey() : request.getAccountKey();

            log.error("Email failed | accountKey={} | to={} | template={} | error={}",
                    resolvedKey, request.getTo(), request.getTemplateName(), e.getMessage());

            persistLog(request, resolvedKey, EmailStatus.FAILED, e.getMessage(), e);

            if (e instanceof EmailSendException ese) {
                throw ese;
            }
            throw new EmailSendException("Failed to send email to: " + request.getTo(), e);
        }
    }

    private String buildFromAddress(EmailAccount account) {
        if (account.getFromName() != null && !account.getFromName().isBlank()) {
            return account.getFromName();
        }
        return account.getUsername();
    }

    private void persistLog(
            EmailRequestDTO request,
            String accountKey,
            EmailStatus status,
            String errorMessage,
            Throwable throwable) {

        String stackTrace = null;
        if (throwable != null) {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            stackTrace = sw.toString();
        }

        EmailLog emailLog = EmailLog.builder()
                .accountKey(accountKey)
                .toAddress(request.getTo())
                .subject(request.getSubject())
                .templateName(request.getTemplateName())
                .status(status)
                .errorMessage(errorMessage)
                .stackTrace(stackTrace)
                .sentAt(LocalDateTime.now())
                .build();
        try {
            emailLogRepository.save(emailLog);
        } catch (Exception ex) {
            log.error("Failed to persist email log | accountKey={} | to={} | status={}",
                    accountKey, request.getTo(), status, ex);
        }
    }
}
