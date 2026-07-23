package com.vof.service.impl;
import com.vof.dto.request.CreatePackageRequest;
import com.vof.dto.response.PackageResponse;
import com.vof.entity.Itinerary;
import com.vof.entity.Meal;
import com.vof.entity.Package;
import com.vof.exception.ResourceNotFoundException;
import com.vof.mapper.PackageMapper;
import com.vof.repository.PackageRepository;
import com.vof.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Service @RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {
    private final PackageRepository packageRepository;
    private final PackageMapper packageMapper;
    @Override @Transactional
    public PackageResponse createPackage(CreatePackageRequest request) {
        Package newPackage = new Package();
        newPackage.setTitle(request.getTitle());
        newPackage.setSlug(generateSlug(request.getTitle()));
        newPackage.setDescription(request.getDescription());
        newPackage.setPrice(request.getPrice());
        newPackage.setDuration(request.getDuration());
        newPackage.setLocation(request.getLocation());
        if (request.getItinerary() != null) {
            Itinerary itinerary = new Itinerary();
            itinerary.setDayNo(request.getItinerary().getDayNo());
            itinerary.setTitle(request.getItinerary().getTitle());
            itinerary.setDescription(request.getItinerary().getDescription());
            itinerary.setAPackage(newPackage);
            List<Itinerary> itineraries = new ArrayList<>();
            itineraries.add(itinerary);
            newPackage.setItinerary(itineraries);
        }
        if (request.getMeals() != null) {
            Meal meal = new Meal();
            meal.setBreakfast(request.getMeals().getBreakfast());
            meal.setLunch(request.getMeals().getLunch());
            meal.setDinner(request.getMeals().getDinner());
            meal.setAPackage(newPackage);
            newPackage.setMeal(meal);
        }
        Package savedPackage = packageRepository.save(newPackage);
        return packageMapper.toPackageResponse(savedPackage);
    }
    @Override @Transactional
    public PackageResponse updatePackage(Long id, CreatePackageRequest request) {
        Package existingPackage = packageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Package not found with id: " + id));
        existingPackage.setTitle(request.getTitle());
        existingPackage.setSlug(generateSlug(request.getTitle()));
        existingPackage.setDescription(request.getDescription());
        existingPackage.setPrice(request.getPrice());
        existingPackage.setDuration(request.getDuration());
        existingPackage.setLocation(request.getLocation());
        if (existingPackage.getItinerary() != null) existingPackage.getItinerary().clear();
        if (request.getItinerary() != null) {
            Itinerary itinerary = new Itinerary();
            itinerary.setDayNo(request.getItinerary().getDayNo());
            itinerary.setTitle(request.getItinerary().getTitle());
            itinerary.setDescription(request.getItinerary().getDescription());
            itinerary.setAPackage(existingPackage);
            if(existingPackage.getItinerary() == null) existingPackage.setItinerary(new ArrayList<>());
            existingPackage.getItinerary().add(itinerary);
        }
        if (request.getMeals() != null) {
            Meal meal = existingPackage.getMeal();
            if (meal == null) { meal = new Meal(); meal.setAPackage(existingPackage); existingPackage.setMeal(meal); }
            meal.setBreakfast(request.getMeals().getBreakfast());
            meal.setLunch(request.getMeals().getLunch());
            meal.setDinner(request.getMeals().getDinner());
        }
        Package updatedPackage = packageRepository.save(existingPackage);
        return packageMapper.toPackageResponse(updatedPackage);
    }
    @Override @Transactional
    public void deletePackage(Long id) {
        Package existingPackage = packageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Package not found with id: " + id));
        if (!existingPackage.getBookings().isEmpty()) throw new IllegalArgumentException("Packages with existing bookings cannot be deleted.");
        packageRepository.delete(existingPackage);
    }
    @Override @Transactional(readOnly = true)
    public List<PackageResponse> getAllPackages() { return packageRepository.findAll().stream().map(packageMapper::toPackageResponse).collect(Collectors.toList()); }
    @Override @Transactional(readOnly = true)
    public PackageResponse getPackageById(Long id) { Package pkg = packageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Package not found with id: " + id)); return packageMapper.toPackageResponse(pkg); }
    private String generateSlug(String title) { if (title == null) return ""; return StringUtils.trimWhitespace(title.toLowerCase()).replaceAll("\\s+", "-"); }
}
