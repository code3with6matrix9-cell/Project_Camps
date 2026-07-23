package com.vof.service.impl;
import com.vof.entity.Review;
import com.vof.repository.ReviewRepository;
import com.vof.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service @RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    @Override public List<Review> getAllReviews() { return reviewRepository.findAll(); }
}
