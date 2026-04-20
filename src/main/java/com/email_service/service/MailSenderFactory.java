package com.email_service.service;

import java.util.Properties;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import com.email_service.entity.EmailAccount;

/**
 * Builds a {@link JavaMailSender} dynamically from an {@link EmailAccount}.
 *
 * Port 465  → direct SSL/TLS (e.g. Hostinger)<br>
 * Port 587  → STARTTLS (e.g. Gmail, Outlook)
 */
@Component
public class MailSenderFactory {

    private static final int SSL_PORT = 465;

    public JavaMailSender build(EmailAccount account) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(account.getSmtpHost());
        mailSender.setPort(account.getSmtpPort());
        mailSender.setUsername(account.getUsername());
        mailSender.setPassword(account.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");
        props.put("mail.smtp.writetimeout", "15000");

        if (account.getSmtpPort() == SSL_PORT) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.trust", account.getSmtpHost());
            props.put("mail.smtp.socketFactory.port", String.valueOf(SSL_PORT));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", account.getSmtpHost());
        }

        return mailSender;
    }
}
