package com.vof.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class CreatePackageRequest {
    @NotBlank @Size(max = 160) private String title;
    @NotBlank @Size(max = 5000) private String description;
    @NotNull @DecimalMin(value = "0.01") private Double price;
    @NotBlank @Size(max = 80) private String duration;
    @NotBlank @Size(max = 160) private String location;
    @Valid private ItineraryRequest itinerary;
    @Valid private MealRequest meals;
}
