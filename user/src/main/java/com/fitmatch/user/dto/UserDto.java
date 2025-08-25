package com.fitmatch.user.dto;

import com.fitmatch.user.enums.Activity;
import com.fitmatch.user.enums.FitnessLevel;
import lombok.Builder;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record UserDto(
    UUID id,
    String email,
    String fullName,
    FitnessLevel fitnessLevel,
    List<Activity> activityInterests,
    Double lat,
    Double lon,
    Integer searchRadiusKm,
    boolean profileCompleted,
    Instant createdAt) {}
