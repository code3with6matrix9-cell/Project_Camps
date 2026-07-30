package com.vof.service.impl;
import com.vof.dto.request.ContactRequest;
import com.vof.entity.ContactMessage;
import com.vof.repository.ContactMessageRepository;
import com.vof.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.vof.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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

    @Override
    @Transactional(readOnly = true)
    public List<ContactMessage> getAllContactMessages() {
        return contactMessageRepository.findAllByOrderByIdDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public ContactMessage getContactMessageById(Long id) {
        return contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message not found with id: " + id));
    }
}
