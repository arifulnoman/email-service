package com.email_service.repository;

import com.email_service.entity.EmailLog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
	
}
