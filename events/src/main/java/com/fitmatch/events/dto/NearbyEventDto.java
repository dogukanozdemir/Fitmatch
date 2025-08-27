package com.fitmatch.events.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearbyEventDto {
  private UUID id;
  private String title;
  private String activity;
  private String fitnessLevel;
  private LocalDateTime startsAt;
  private Integer capacity;
  private Integer participantCount;
  private Double distance;
  private Double lat;
  private Double lng;
}
