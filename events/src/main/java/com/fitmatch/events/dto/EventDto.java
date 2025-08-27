package com.fitmatch.events.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fitmatch.common.enums.Activity;
import com.fitmatch.common.enums.FitnessLevel;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

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
