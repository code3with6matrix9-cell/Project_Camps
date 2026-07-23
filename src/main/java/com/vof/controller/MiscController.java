package com.vof.controller;
import com.vof.dto.request.ContactRequest;
import com.vof.dto.response.CommonApiResponse;
import com.vof.service.ContactService;
import com.vof.service.FaqService;
import com.vof.service.GalleryService;
import com.vof.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class MiscController {
    private final GalleryService galleryService;
    private final ReviewService reviewService;
    private final FaqService faqService;
    private final ContactService contactService;
    @GetMapping("/gallery") public ResponseEntity<CommonApiResponse> getGallery() { return ResponseEntity.ok(CommonApiResponse.builder().success(true).data(galleryService.getAllGalleryImages()).build()); }
    @GetMapping("/reviews") public ResponseEntity<CommonApiResponse> getReviews() { return ResponseEntity.ok(CommonApiResponse.builder().success(true).data(reviewService.getAllReviews()).build()); }
    @GetMapping("/faqs") public ResponseEntity<CommonApiResponse> getFaqs() { return ResponseEntity.ok(CommonApiResponse.builder().success(true).data(faqService.getAllFaqs()).build()); }
    @PostMapping("/contact")
    public ResponseEntity<CommonApiResponse> submitContactForm(@Valid @RequestBody ContactRequest request) {
        contactService.saveContactMessage(request);
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).message("Your message has been received.").build());
    }
}
