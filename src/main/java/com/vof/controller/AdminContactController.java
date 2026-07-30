package com.vof.controller;

import com.vof.entity.ContactMessage;
import com.vof.dto.response.CommonApiResponse;
import com.vof.service.ContactService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/contact-messages")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminContactController {

    private final ContactService contactService;

    @GetMapping
    public ResponseEntity<CommonApiResponse> getAllContactMessages() {
        List<ContactMessage> messages = contactService.getAllContactMessages();
        return ResponseEntity.ok(CommonApiResponse.builder()
                .success(true)
                .message("All contact messages retrieved.")
                .data(messages)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonApiResponse> getContactMessageById(@PathVariable Long id) {
        ContactMessage message = contactService.getContactMessageById(id);
        return ResponseEntity.ok(CommonApiResponse.builder()
                .success(true)
                .message("Contact message retrieved successfully.")
                .data(message)
                .build());
    }
}
