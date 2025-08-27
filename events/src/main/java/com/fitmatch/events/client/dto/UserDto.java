package com.fitmatch.events.client.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UserDto(
    UUID id,
    String email,
    String fullName,
    String fitnessLevel,
    List<String> activityInterests,
    Double lat,
    Double lon,
    Integer searchRadiusKm,
    boolean profileCompleted,
    Instant createdAt) {}
