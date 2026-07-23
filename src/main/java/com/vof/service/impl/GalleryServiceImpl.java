package com.vof.service.impl;
import com.vof.entity.GalleryImage;
import com.vof.repository.GalleryImageRepository;
import com.vof.service.GalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service @RequiredArgsConstructor
public class GalleryServiceImpl implements GalleryService {
    private final GalleryImageRepository galleryImageRepository;
    @Override public List<GalleryImage> getAllGalleryImages() { return galleryImageRepository.findAll(); }
}
