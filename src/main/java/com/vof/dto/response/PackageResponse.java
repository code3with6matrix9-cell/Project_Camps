package com.vof.dto.response;
import lombok.Data;
import java.util.List;
@Data
public class PackageResponse {
    private Long id; private String title; private String description; private Double price;
    private String duration; private String location;
    private List<String> images;
    private List<ItineraryResponse> itinerary;
    private MealResponse meals;
}
