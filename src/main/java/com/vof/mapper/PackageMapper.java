package com.vof.mapper;
import com.vof.dto.response.ItineraryResponse;
import com.vof.dto.response.MealResponse;
import com.vof.dto.response.PackageResponse;
import com.vof.entity.Itinerary;
import com.vof.entity.Meal;
import com.vof.entity.Package;
import com.vof.entity.PackageImage;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.stream.Collectors;
@Component
public class PackageMapper {
    public PackageResponse toPackageResponse(Package pkg) {
        if (pkg == null) return null;
        PackageResponse response = new PackageResponse();
        response.setId(pkg.getId()); response.setTitle(pkg.getTitle()); response.setDescription(pkg.getDescription());
        response.setPrice(pkg.getPrice()); response.setDuration(pkg.getDuration()); response.setLocation(pkg.getLocation());
        response.setImages(pkg.getImages() != null ? pkg.getImages().stream().map(PackageImage::getImageUrl).collect(Collectors.toList()) : Collections.emptyList());
        response.setItinerary(pkg.getItinerary() != null ? pkg.getItinerary().stream().map(this::toItineraryResponse).collect(Collectors.toList()) : Collections.emptyList());
        response.setMeals(toMealResponse(pkg.getMeal()));
        return response;
    }
    public ItineraryResponse toItineraryResponse(Itinerary itinerary) {
        if (itinerary == null) return null;
        ItineraryResponse response = new ItineraryResponse();
        response.setDayNo(itinerary.getDayNo()); response.setTitle(itinerary.getTitle()); response.setDescription(itinerary.getDescription());
        return response;
    }
    public MealResponse toMealResponse(Meal meal) {
        if (meal == null) return null;
        MealResponse response = new MealResponse();
        response.setBreakfast(meal.getBreakfast()); response.setLunch(meal.getLunch()); response.setDinner(meal.getDinner());
        return response;
    }
}
