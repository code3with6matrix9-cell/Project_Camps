package com.vof.service.impl;
import com.vof.entity.Faq;
import com.vof.repository.FaqRepository;
import com.vof.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service @RequiredArgsConstructor
public class FaqServiceImpl implements FaqService {
    private final FaqRepository faqRepository;
    @Override public List<Faq> getAllFaqs() { return faqRepository.findAll(); }
}
