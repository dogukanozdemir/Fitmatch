package com.fitmatch.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fitmatch.common.enums.Activity;
import com.fitmatch.common.enums.FitnessLevel;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public record CreateEventRequest(
    @NotBlank @Size(max = 140) String title,
    @Size(max = 2000) String description,
    @NotNull Activity activityType,
    @NotNull FitnessLevel fitnessLevel,
    @NotNull @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy HH:mm")
        LocalDateTime startsAt,
    @Min(2) @Max(10) int capacity,
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double lat,
    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double lng) {}
