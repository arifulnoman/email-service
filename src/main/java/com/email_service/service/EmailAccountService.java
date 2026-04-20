package com.email_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.email_service.dto.EmailAccountRequestDTO;
import com.email_service.dto.EmailAccountResponseDTO;
import com.email_service.entity.EmailAccount;
import com.email_service.exception.EmailSendException;
import com.email_service.repository.EmailAccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAccountService {

    private final EmailAccountRepository emailAccountRepository;

    private final CacheManager cacheManager;

    /**
     * Retrieves an active email account by key.
     *
     * @param accountKey the logical key identifying the sender account
     * @return the matching {@link EmailAccount}
     * @throws EmailSendException if no active account is found for the given key
     */
    public EmailAccount getByAccountKey(String accountKey) {
        Cache cache = cacheManager.getCache("emailAccounts");
        boolean cacheHit = cache != null && cache.get(accountKey) != null;

        if (cacheHit) {
            log.info("Cache hit — serving email account from cache | accountKey={}", accountKey);
        } else {
            log.info("Cache miss — loading email account from DB | accountKey={}", accountKey);
        }

        return self.loadByAccountKey(accountKey);
    }

    @Cacheable(value = "emailAccounts", key = "#accountKey")
    public EmailAccount loadByAccountKey(String accountKey) {
        return emailAccountRepository.findByAccountKeyAndActive(accountKey, true)
                .orElseThrow(() -> {
                    log.error("Email account not found or inactive | accountKey={}", accountKey);
                    return new EmailSendException("No active email account found for key: " + accountKey);
                });
    }

    /**
     * Registers a new email account and immediately caches it.
     *
     * @param request the account details
     * @return the saved account as a response DTO
     */
    @CachePut(value = "emailAccounts", key = "#result.accountKey")
    public EmailAccount create(EmailAccountRequestDTO request) {
        validateEmailAccountRequest(request);

        if (emailAccountRepository.existsByAccountKey(request.getAccountKey())) {
            throw new IllegalArgumentException(
                    "Email account with key '" + request.getAccountKey() + "' already exists");
        }

        EmailAccount account = EmailAccount.builder()
                .accountKey(request.getAccountKey())
                .smtpHost(request.getSmtpHost())
                .smtpPort(request.getSmtpPort())
                .username(request.getUsername())
                .password(request.getPassword())
                .fromName(request.getFromName())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        EmailAccount saved = emailAccountRepository.save(account);
        log.info("Email account created and cached | accountKey={}", saved.getAccountKey());
        return saved;
    }

    private void validateEmailAccountRequest(EmailAccountRequestDTO request) {
        if (request.getAccountKey() == null || request.getAccountKey().isBlank()) {
            throw new IllegalArgumentException("Account key is required");
        }
        if (request.getSmtpHost() == null || request.getSmtpHost().isBlank()) {
            throw new IllegalArgumentException("SMTP Host is required");
        }
        if (request.getSmtpPort() == null || request.getSmtpPort() <= 0) {
            throw new IllegalArgumentException("Valid SMTP Port is required");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (request.getFromName() == null || request.getFromName().isBlank()) {
            throw new IllegalArgumentException("From Name is required");
        }
    }

    /**
     * Updates an existing email account and evicts the old cached entry.
     * The next read will re-fetch fresh data from the DB.
     *
     * @param accountKey the key of the account to update
     * @param request    the new account details
     * @return the updated account response DTO
     */
    @CacheEvict(value = "emailAccounts", key = "#accountKey")
    public EmailAccount update(String accountKey, EmailAccountRequestDTO request) {
        EmailAccount account = emailAccountRepository.findByAccountKeyAndActive(accountKey, true)
                .orElseThrow(() -> new EmailSendException(
                        "No active email account found for key: " + accountKey));

        if (request.getSmtpHost() != null) {
            account.setSmtpHost(request.getSmtpHost());
        }
        if (request.getSmtpPort() != null) {
            account.setSmtpPort(request.getSmtpPort());
        }
        if (request.getUsername() != null) {
            account.setUsername(request.getUsername());
        }
        if (request.getPassword() != null) {
            account.setPassword(request.getPassword());
        }
        if (request.getFromName() != null) {
            account.setFromName(request.getFromName());
        }
        if (request.getActive() != null) {
            account.setActive(request.getActive());
        }

        EmailAccount updated = emailAccountRepository.save(account);
        log.info("Email account updated and cache evicted | accountKey={}", accountKey);
        return updated;
    }

    /**
     * Soft-deletes (deactivates) an email account and immediately evicts
     * its cache entry to prevent stale sends via deactivated credentials.
     *
     * @param accountKey the key of the account to deactivate
     */
    @CacheEvict(value = "emailAccounts", key = "#accountKey")
    public void delete(String accountKey) {
        EmailAccount account = emailAccountRepository.findByAccountKeyAndActive(accountKey, true)
                .orElseThrow(() -> new EmailSendException(
                        "No active email account found for key: " + accountKey));

        account.setActive(false);
        emailAccountRepository.save(account);
        log.info("Email account deactivated and cache evicted | accountKey={}", accountKey);
    }

    /**
     * Lists all email accounts (active and inactive), without passwords.
     */
    public List<EmailAccountResponseDTO> listAll() {
        return emailAccountRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Autowired
    @Lazy
    private EmailAccountService self;

    /**
     * Fetches a single account by key (active only), without password.
     */
    public EmailAccountResponseDTO getResponseByKey(String accountKey) {
        return toResponseDTO(getByAccountKey(accountKey));
    }

    public EmailAccountResponseDTO toResponseDTO(EmailAccount account) {
        return EmailAccountResponseDTO.builder()
                .id(account.getId())
                .accountKey(account.getAccountKey())
                .smtpHost(account.getSmtpHost())
                .smtpPort(account.getSmtpPort())
                .username(account.getUsername())
                .fromName(account.getFromName())
                .active(account.getActive())
                .build();
    }
}
