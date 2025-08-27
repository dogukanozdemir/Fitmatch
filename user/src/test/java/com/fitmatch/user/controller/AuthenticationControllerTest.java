package com.fitmatch.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitmatch.user.dto.AuthenticationResponse;
import com.fitmatch.user.dto.LoginRequest;
import com.fitmatch.user.dto.RegisterRequest;
import com.fitmatch.user.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(controllers = AuthenticationController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFiltsderAutoConfiguration.class})
class AuthenticationControllerTest {

  @Autowired private MockMvc mockMvc;

  @Mock private AuthenticationService authenticationService;

  @Autowired private ObjectMapper objectMapper;

  private LoginRequest validLoginRequest;
  private RegisterRequest validRegisterRequest;
  private AuthenticationResponse authResponse;

  @BeforeEach
  void setUp() {
    validLoginRequest = new LoginRequest("john.doe@example.com", "password123");
    validRegisterRequest = new RegisterRequest("john.doe@example.com", "John Doe", "password123");
    authResponse =
        AuthenticationResponse.builder()
            .email("john.doe@example.com")
            .token("jwt-token-123")
            .build();
  }

  @Test
  void login_WithValidCredentials_ShouldReturnOk() throws Exception {
    // Given
    when(authenticationService.login(any(LoginRequest.class))).thenReturn(authResponse);

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.email").value("john.doe@example.com"))
        .andExpect(jsonPath("$.token").value("jwt-token-123"));
  }

  @Test
  void login_WithNonExistentUser_ShouldReturnNotFound() throws Exception {
    // Given
    when(authenticationService.login(any(LoginRequest.class)))
        .thenThrow(
            new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User with name john.doe@example.com not found"));

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
    // Given
    when(authenticationService.login(any(LoginRequest.class)))
        .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Credentials"));

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void register_WithValidData_ShouldReturnCreated() throws Exception {
    // Given
    when(authenticationService.register(any(RegisterRequest.class))).thenReturn(authResponse);

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.email").value("john.doe@example.com"))
        .andExpect(jsonPath("$.token").value("jwt-token-123"));
  }

  @Test
  void register_WithExistingUser_ShouldReturnBadRequest() throws Exception {
    // Given
    when(authenticationService.register(any(RegisterRequest.class)))
        .thenThrow(
            new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Customer with this email already exists"));

    // When & Then
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
        .andExpect(status().isBadRequest());
  }
}
