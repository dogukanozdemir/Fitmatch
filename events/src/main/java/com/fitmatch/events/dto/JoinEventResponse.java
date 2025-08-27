package com.fitmatch.events.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;


@Builder
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public record JoinEventResponse(String message, EventDto event) {}
