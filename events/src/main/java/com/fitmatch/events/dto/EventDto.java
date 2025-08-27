package com.fitmatch.events.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fitmatch.common.enums.Activity;
import com.fitmatch.common.enums.FitnessLevel;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public record EventDto(
    UUID eventId,
    String title,
    String description,
    Activity activity,
    FitnessLevel fitnessLevel,
    LocalDateTime startsAt,
    Integer capacity,
    int participantCount) {}
