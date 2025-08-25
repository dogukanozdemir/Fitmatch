package com.fitmatch.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthenticatedUser {

    private UUID id;
    private String email;
}
