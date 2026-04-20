package com.email_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.email_service.entity.EmailAccount;

public interface EmailAccountRepository extends JpaRepository<EmailAccount, Long> {

    Optional<EmailAccount> findByAccountKeyAndActive(String accountKey, boolean active);

    boolean existsByAccountKey(String accountKey);
}
