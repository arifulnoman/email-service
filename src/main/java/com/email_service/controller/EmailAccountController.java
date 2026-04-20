package com.email_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.email_service.dto.EmailAccountRequestDTO;
import com.email_service.dto.EmailAccountResponseDTO;
import com.email_service.entity.EmailAccount;
import com.email_service.service.EmailAccountService;

import lombok.RequiredArgsConstructor;

/**
 * All endpoints are protected by ApiKeyAuthFilter
 */
@RestController
@RequestMapping("/api/email-accounts")
@RequiredArgsConstructor
public class EmailAccountController {

    private final EmailAccountService emailAccountService;

    @PostMapping
    public ResponseEntity<EmailAccountResponseDTO> create(@RequestBody EmailAccountRequestDTO request) {
        EmailAccount saved = emailAccountService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(emailAccountService.toResponseDTO(saved));
    }

    @GetMapping
    public ResponseEntity<List<EmailAccountResponseDTO>> listAll() {
        return ResponseEntity.ok(emailAccountService.listAll());
    }

    @GetMapping("/{accountKey}")
    public ResponseEntity<EmailAccountResponseDTO> getOne(@PathVariable String accountKey) {
        return ResponseEntity.ok(emailAccountService.getResponseByKey(accountKey));
    }

    @PutMapping("/{accountKey}")
    public ResponseEntity<EmailAccountResponseDTO> update(
            @PathVariable String accountKey,
            @RequestBody EmailAccountRequestDTO request) {
        EmailAccount updated = emailAccountService.update(accountKey, request);
        return ResponseEntity.ok(emailAccountService.toResponseDTO(updated));
    }

    @DeleteMapping("/{accountKey}")
    public ResponseEntity<Void> delete(@PathVariable String accountKey) {
        emailAccountService.delete(accountKey);
        return ResponseEntity.noContent().build();
    }
}
