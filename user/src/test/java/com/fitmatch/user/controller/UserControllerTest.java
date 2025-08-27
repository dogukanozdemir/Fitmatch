package com.fitmatch.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitmatch.common.dto.UserDto;
import com.fitmatch.common.enums.Activity;
import com.fitmatch.common.enums.FitnessLevel;
import com.fitmatch.user.dto.CompleteProfileRequest;
import com.fitmatch.user.service.UserService;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

class UserControllerTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @Mock private UserService userService;

  @InjectMocks private UserController controller;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    objectMapper = new ObjectMapper();
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  void getUser_ok() throws Exception {
    UUID id = UUID.randomUUID();
    UserDto dto =
        UserDto.builder()
            .id(id)
            .email("x@y.com")
            .fullName("Test User")
            .activityInterests(Set.of(Activity.RUNNING))
            .fitnessLevel(FitnessLevel.BEGINNER)
            .lat(41.0)
            .lon(29.0)
            .searchRadiusKm(10)
            .profileCompleted(true)
            .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
            .build();

    when(userService.getUserById(eq(id))).thenReturn(dto);

    mockMvc
        .perform(get("/api/users/{id}", id.toString()))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.email").value("x@y.com"))
        .andExpect(jsonPath("$.fullName").value("Test User"))
        .andExpect(jsonPath("$.lat").value(41.0))
        .andExpect(jsonPath("$.lon").value(29.0));

    verify(userService).getUserById(eq(id));
  }

  @Test
  void getUser_notFound() throws Exception {
    UUID id = UUID.randomUUID();
    when(userService.getUserById(eq(id)))
        .thenThrow(
            new ResponseStatusException(HttpStatus.NOT_FOUND, "user with id does not exist"));

    mockMvc.perform(get("/api/users/{id}", id.toString())).andExpect(status().isNotFound());
  }

  @Test
  void completeProfile_ok() throws Exception {
    CompleteProfileRequest req =
        new CompleteProfileRequest(
            FitnessLevel.INTERMEDIATE,
            Set.of(Activity.SWIMMING, Activity.CYCLING),
            41.05,
            29.01,
            25);

    UserDto resp =
        UserDto.builder()
            .id(UUID.randomUUID())
            .email("x@y.com")
            .fullName("Test User")
            .activityInterests(req.activityInterests())
            .fitnessLevel(req.fitnessLevel())
            .lat(req.latitude())
            .lon(req.longitude())
            .searchRadiusKm(req.searchRadiusKm())
            .profileCompleted(true)
            .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
            .build();

    when(userService.completeProfile(any(CompleteProfileRequest.class))).thenReturn(resp);

    String body = objectMapper.writeValueAsString(req);

    mockMvc
        .perform(post("/api/users/profile").contentType(APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
        .andExpect(jsonPath("$.email").value("x@y.com"))
        .andExpect(jsonPath("$.profileCompleted").value(true));

    verify(userService).completeProfile(any(CompleteProfileRequest.class));
  }

  @Test
  void completeProfile_unauthorized() throws Exception {
    CompleteProfileRequest req =
        new CompleteProfileRequest(FitnessLevel.BEGINNER, Set.of(Activity.RUNNING), 41.0, 29.0, 10);

    when(userService.completeProfile(any(CompleteProfileRequest.class)))
        .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

    String body = objectMapper.writeValueAsString(req);

    mockMvc
        .perform(post("/api/users/profile").contentType(APPLICATION_JSON).content(body))
        .andExpect(status().isUnauthorized());
  }
}
