package com.fitmatch.events.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public interface NearbyEventView {
  UUID getId();

  String getTitle();

  String getActivity();

  String getFitnessLevel();

  LocalDateTime getStartsAt(); // <- works with TIMESTAMPTZ

  Integer getCapacity();

  Integer getParticipantCount();

  Double getDistance();

  Double getLat();

  Double getLng();
}
