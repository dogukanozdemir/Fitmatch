package com.fitmatch.user.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticatedUser {

  private UUID id;
  private String email;
}
