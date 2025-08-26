package com.fitmatch.user.controller;

import com.fitmatch.user.dto.AuthenticationResponse;
import com.fitmatch.user.dto.LoginRequest;
import com.fitmatch.user.dto.RegisterRequest;
import com.fitmatch.user.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService authenticationService;

  @PostMapping("/login")
  public ResponseEntity<AuthenticationResponse> login(
      @RequestBody @Valid LoginRequest loginRequest) {
    return ResponseEntity.ok(authenticationService.login(loginRequest));
  }

  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(
      @RequestBody @Valid RegisterRequest registerRequest) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(authenticationService.register(registerRequest));
  }
}
