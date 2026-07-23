package com.vof.dto.request;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class ItineraryRequest {
    @Min(1) private int dayNo;
    @NotBlank @Size(max = 160) private String title;
    @NotBlank @Size(max = 5000) private String description;
}
