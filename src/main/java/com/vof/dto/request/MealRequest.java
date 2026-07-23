package com.vof.dto.request;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class MealRequest {
    @Size(max = 500) private String breakfast;
    @Size(max = 500) private String lunch;
    @Size(max = 500) private String dinner;
}
