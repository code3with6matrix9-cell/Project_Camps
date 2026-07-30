package com.vof.service;

import com.vof.dto.request.ContactRequest;
import com.vof.entity.ContactMessage;
import java.util.List;

public interface ContactService {
    void saveContactMessage(ContactRequest request);
    List<ContactMessage> getAllContactMessages();
    ContactMessage getContactMessageById(Long id);
}
