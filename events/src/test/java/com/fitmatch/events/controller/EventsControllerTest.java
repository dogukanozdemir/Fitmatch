package com.fitmatch.events.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fitmatch.common.enums.Activity;
import com.fitmatch.common.enums.FitnessLevel;
import com.fitmatch.events.EventsController;
import com.fitmatch.events.EventsService;
import com.fitmatch.events.dto.CreateEventRequest;
import com.fitmatch.events.dto.EventDto;
import com.fitmatch.events.dto.GetNearbyEventsResponse;
import com.fitmatch.events.dto.JoinEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EventsControllerTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @Mock private EventsService eventsService;

  @InjectMocks private EventsController controller;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
  }
  @Test
  void createEvent_ok() throws Exception {
    CreateEventRequest req =
        new CreateEventRequest(
            "Title",
            "Desc",
            Activity.RUNNING,
            FitnessLevel.BEGINNER,
            LocalDateTime.now().plusDays(1),
            10,
            29.0,
            41.0);

    EventDto dto =
        EventDto.builder()
            .eventId(UUID.randomUUID())
            .title("Title")
            .description("Desc")
            .activity(Activity.RUNNING)
            .fitnessLevel(FitnessLevel.BEGINNER)
            .startsAt(req.startsAt())
            .capacity(10)
            .participantCount(1)
            .build();

    when(eventsService.createEvent(any(CreateEventRequest.class))).thenReturn(dto);

    mockMvc
        .perform(
            post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(
            status()
                .isOk()) // method is annotated with @ResponseStatus(CREATED) but returns
                         // ResponseEntity.ok(...)
        .andExpect(jsonPath("$.title").value("Title"))
        .andExpect(jsonPath("$.capacity").value(10));

    verify(eventsService).createEvent(any(CreateEventRequest.class));
  }

  @Test
  void nearby_ok() throws Exception {
    GetNearbyEventsResponse r1 =
        GetNearbyEventsResponse.builder()
            .compatibilityScore(87.5)
            .event(null) // not needed for this check
            .build();
    when(eventsService.getNearbyEvents()).thenReturn(List.of(r1));

    mockMvc
        .perform(get("/api/events/nearby"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].compatibilityScore").value(87.5));

    verify(eventsService).getNearbyEvents();
  }

  @Test
  void delete_noContent() throws Exception {
    UUID id = UUID.randomUUID();

    mockMvc.perform(delete("/api/events/{eventId}", id)).andExpect(status().isNoContent());

    verify(eventsService).deleteEvent(id);
  }

  @Test
  void join_ok() throws Exception {
    UUID id = UUID.randomUUID();
    JoinEventResponse resp = JoinEventResponse.builder().message("ok").build();
    when(eventsService.joinEvent(id)).thenReturn(resp);

    mockMvc
        .perform(post("/api/events/{eventId}/join", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("ok"));

    verify(eventsService).joinEvent(id);
  }

  @Test
  void leave_noContent() throws Exception {
    UUID id = UUID.randomUUID();

    mockMvc.perform(delete("/api/events/{eventId}/leave", id)).andExpect(status().isNoContent());

    verify(eventsService).leaveEvent(id);
  }
}
