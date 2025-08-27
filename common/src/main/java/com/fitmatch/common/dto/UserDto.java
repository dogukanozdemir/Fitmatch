package com.fitmatch.common.dto;

import com.fitmatch.common.enums.Activity;
import com.fitmatch.common.enums.FitnessLevel;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UserDto(
    UUID id,
    String email,
    String fullName,
    FitnessLevel fitnessLevel,
    Set<Activity> activityInterests,
    Double lat,
    Double lon,
    Integer searchRadiusKm,
    boolean profileCompleted,
    Instant createdAt) {}
