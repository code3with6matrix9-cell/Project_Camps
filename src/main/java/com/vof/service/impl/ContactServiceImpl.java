package com.vof.service.impl;
import com.vof.dto.request.ContactRequest;
import com.vof.entity.ContactMessage;
import com.vof.repository.ContactMessageRepository;
import com.vof.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {
    private final ContactMessageRepository contactMessageRepository;
    @Override @Transactional
    public void saveContactMessage(ContactRequest request) {
        ContactMessage message = new ContactMessage();
        message.setName(request.getName());
        message.setEmail(request.getEmail());
        message.setPhone(request.getPhone());
        message.setMessage(request.getMessage());
        contactMessageRepository.save(message);
    }
}
