package com.fitmatch.user.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public record RegisterRequest(
    @NotNull @NotBlank String email,
    @NotNull @NotBlank String fullName,
    @NotNull @NotBlank String password) {}
