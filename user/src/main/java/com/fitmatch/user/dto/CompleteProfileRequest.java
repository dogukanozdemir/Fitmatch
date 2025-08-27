package com.fitmatch.user.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fitmatch.common.enums.Activity;
import com.fitmatch.common.enums.FitnessLevel;
import jakarta.validation.constraints.*;
import java.util.Set;

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public record CompleteProfileRequest(
    @NotNull @NotBlank FitnessLevel fitnessLevel,
    @NotNull
        @NotBlank
        @Size(min = 1, max = 3, message = "activity interests can include at most 3 activities")
        Set<Activity> activityInterests,
    @NotNull @Min(-90) @Max(90) Double latitude,
    @NotNull @Min(-180) @Max(180) Double longitude,
    @Positive @Max(200) Integer searchRadiusKm) {}
