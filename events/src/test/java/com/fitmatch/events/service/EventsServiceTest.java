package com.fitmatch.events.service;

import com.fitmatch.common.enums.Activity;
import com.fitmatch.common.enums.FitnessLevel;
import com.fitmatch.events.EventsService;
import com.fitmatch.events.client.UserClient;
import com.fitmatch.events.client.dto.UserDto;
import com.fitmatch.events.dto.CreateEventRequest;
import com.fitmatch.events.dto.EventDto;
import com.fitmatch.events.dto.GetNearbyEventsResponse;
import com.fitmatch.events.dto.NearbyEventView;
import com.fitmatch.events.entity.Event;
import com.fitmatch.events.entity.EventParticipant;
import com.fitmatch.events.repository.EventParticipantRepository;
import com.fitmatch.events.repository.EventsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventsServiceTest {

  @Mock private UserClient userClient;

  @Mock private EventsRepository eventsRepository;

  @Mock private GeometryFactory geometryFactory;

  @Mock private EventParticipantRepository eventParticipantRepository;

  @Mock private EntityManager entityManager;

  @InjectMocks private EventsService eventsService;

  @AfterEach
  void clearCtx() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthUser(UUID userId) {
    Authentication auth = mock(Authentication.class);
    when(auth.getDetails()).thenReturn(userId.toString());
    SecurityContextHolder.setContext(new SecurityContextImpl(auth));
  }

  @Test
  void createEvent_ok() {
    UUID userId = UUID.randomUUID();
    setAuthUser(userId);
    when(userClient.getById(userId.toString()))
        .thenReturn(
            createUser(
                userId, 41.0, 29.0, 10, true, FitnessLevel.BEGINNER, List.of(Activity.RUNNING)));

    // Create point
    Point point = mock(Point.class);
    doNothing().when(point).setSRID(4326);
    when(geometryFactory.createPoint(any(Coordinate.class))).thenReturn(point);

    // Save event
    Event saved =
        Event.builder()
            .id(UUID.randomUUID())
            .organizerId(userId)
            .title("T")
            .description("D")
            .activity(Activity.RUNNING)
            .fitnessLevel(FitnessLevel.BEGINNER)
            .startsAt(LocalDateTime.now().plusDays(1))
            .capacity(10)
            .participantCount(1)
            .location(point)
            .build();
    when(eventsRepository.save(any(Event.class))).thenReturn(saved);

    CreateEventRequest req =
        new CreateEventRequest(
            "T",
            "D",
            Activity.RUNNING,
            FitnessLevel.BEGINNER,
            LocalDateTime.now().plusDays(1),
            10,
            29.0,
            41.0);

    EventDto dto = eventsService.createEvent(req);

    assertThat(dto.title()).isEqualTo("T");
    assertThat(dto.activity()).isEqualTo(Activity.RUNNING);
    verify(eventsRepository).save(any(Event.class));
  }

  @Test
  void joinEvent_ok_incrementsCount() {
    UUID userId = UUID.randomUUID();
    setAuthUser(userId);
    when(userClient.getById(userId.toString()))
        .thenReturn(
            createUser(
                userId, 41.0, 29.0, 10, true, FitnessLevel.BEGINNER, List.of(Activity.RUNNING)));

    UUID eventId = UUID.randomUUID();
    Event event =
        Event.builder()
            .id(eventId)
            .organizerId(UUID.randomUUID())
            .title("E")
            .activity(Activity.RUNNING)
            .fitnessLevel(FitnessLevel.BEGINNER)
            .startsAt(LocalDateTime.now().plusHours(2))
            .capacity(2)
            .participantCount(0)
            .build();

    when(entityManager.find(Event.class, eventId, LockModeType.PESSIMISTIC_WRITE))
        .thenReturn(event);
    when(eventParticipantRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(false);
    when(eventsRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

    var resp = eventsService.joinEvent(eventId);

    assertThat(resp.event().participantCount()).isEqualTo(1);
    verify(eventParticipantRepository).save(any(EventParticipant.class));
    verify(eventsRepository).save(event);
  }

  @Test
  void joinEvent_alreadyAttending_400() {
    UUID userId = UUID.randomUUID();
    setAuthUser(userId);
    when(userClient.getById(userId.toString()))
        .thenReturn(
            createUser(
                userId, 41.0, 29.0, 10, true, FitnessLevel.BEGINNER, List.of(Activity.RUNNING)));

    UUID eventId = UUID.randomUUID();
    Event event =
        Event.builder()
            .id(eventId)
            .organizerId(UUID.randomUUID())
            .startsAt(LocalDateTime.now().plusHours(2))
            .capacity(10)
            .participantCount(3)
            .build();

    when(entityManager.find(eq(Event.class), eq(eventId), eq(LockModeType.PESSIMISTIC_WRITE)))
        .thenReturn(event);
    when(eventParticipantRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(true);

    assertThatThrownBy(() -> eventsService.joinEvent(eventId))
        .isInstanceOf(ResponseStatusException.class)
        .extracting("statusCode")
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void joinEvent_fullCapacity_400() {
    UUID userId = UUID.randomUUID();
    setAuthUser(userId);
    when(userClient.getById(userId.toString()))
        .thenReturn(
            createUser(
                userId, 41.0, 29.0, 10, true, FitnessLevel.BEGINNER, List.of(Activity.RUNNING)));

    UUID eventId = UUID.randomUUID();
    Event event =
        Event.builder()
            .id(eventId)
            .organizerId(UUID.randomUUID())
            .startsAt(LocalDateTime.now().plusHours(2))
            .capacity(2)
            .participantCount(2)
            .build();

    when(entityManager.find(eq(Event.class), eq(eventId), eq(LockModeType.PESSIMISTIC_WRITE)))
        .thenReturn(event);
    when(eventParticipantRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(false);

    assertThatThrownBy(() -> eventsService.joinEvent(eventId))
        .isInstanceOf(ResponseStatusException.class)
        .extracting("statusCode")
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void deleteEvent_onlyOrganizerCanDelete() {
    UUID organizer = UUID.randomUUID();
    UUID otherUser = UUID.randomUUID();
    setAuthUser(otherUser);
    when(userClient.getById(otherUser.toString()))
        .thenReturn(createUser(otherUser, 41.0, 29.0, 10, true, FitnessLevel.BEGINNER, List.of()));

    UUID eventId = UUID.randomUUID();
    Event event = Event.builder().id(eventId).organizerId(organizer).build();
    when(eventsRepository.findById(eventId)).thenReturn(Optional.of(event));

    assertThatThrownBy(() -> eventsService.deleteEvent(eventId))
        .isInstanceOf(ResponseStatusException.class)
        .extracting("statusCode")
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void leaveEvent_organizerCannotLeave() {
    UUID organizer = UUID.randomUUID();
    setAuthUser(organizer);
    when(userClient.getById(organizer.toString()))
        .thenReturn(createUser(organizer, 41.0, 29.0, 10, true, FitnessLevel.BEGINNER, List.of()));

    UUID eventId = UUID.randomUUID();
    Event event = Event.builder().id(eventId).organizerId(organizer).build();
    when(eventsRepository.findById(eventId)).thenReturn(Optional.of(event));

    assertThatThrownBy(() -> eventsService.leaveEvent(eventId))
        .isInstanceOf(ResponseStatusException.class)
        .extracting("statusCode")
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void getNearbyEvents_scoresAndSorts() {
    UUID userId = UUID.randomUUID();
    setAuthUser(userId);
    when(userClient.getById(userId.toString()))
        .thenReturn(
            createUser(
                userId, 41.0, 29.0, 20, true, FitnessLevel.BEGINNER, List.of(Activity.RUNNING)));

    NearbyEventView v1 = mock(NearbyEventView.class);
    when(v1.getId()).thenReturn(UUID.randomUUID());
    when(v1.getTitle()).thenReturn("Run A");
    when(v1.getActivity()).thenReturn(Activity.RUNNING.name());
    when(v1.getFitnessLevel()).thenReturn(FitnessLevel.BEGINNER.name());
    when(v1.getStartsAt()).thenReturn(LocalDateTime.now().plusDays(1));
    when(v1.getCapacity()).thenReturn(10);
    when(v1.getParticipantCount()).thenReturn(2);
    when(v1.getDistance()).thenReturn(500.0); // meters
    when(v1.getLat()).thenReturn(41.001);
    when(v1.getLng()).thenReturn(29.001);

    NearbyEventView v2 = mock(NearbyEventView.class);
    when(v2.getId()).thenReturn(UUID.randomUUID());
    when(v2.getTitle()).thenReturn("Swim B");
    when(v2.getActivity()).thenReturn(Activity.SWIMMING.name());
    when(v2.getFitnessLevel()).thenReturn(FitnessLevel.INTERMEDIATE.name());
    when(v2.getStartsAt()).thenReturn(LocalDateTime.now().plusDays(1));
    when(v2.getCapacity()).thenReturn(20);
    when(v2.getParticipantCount()).thenReturn(5);
    when(v2.getDistance()).thenReturn(1500.0);
    when(v2.getLat()).thenReturn(41.01);
    when(v2.getLng()).thenReturn(29.01);

    when(eventsRepository.findNearbyEvents(41.0, 29.0, 20_000.0)).thenReturn(List.of(v1, v2));

    List<GetNearbyEventsResponse> out = eventsService.getNearbyEvents();

    assertThat(out).hasSize(2);
    assertThat(out.get(0).compatibilityScore()).isBetween(0.0, 100.0);
    assertThat(out.get(1).compatibilityScore()).isBetween(0.0, 100.0);
    // v1 should likely rank higher due to same activity + same fitness + closer distance
    assertThat(out.get(0).event().getTitle()).isEqualTo("Run A");
  }

  @Test
  void getNearbyEvents_requiresCompletedProfile() {
    UUID userId = UUID.randomUUID();
    setAuthUser(userId);
    when(userClient.getById(userId.toString()))
        .thenReturn(createUser(userId, 41.0, 29.0, 10, false, FitnessLevel.BEGINNER, List.of()));

    assertThatThrownBy(() -> eventsService.getNearbyEvents())
        .isInstanceOf(ResponseStatusException.class)
        .extracting("statusCode")
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  private UserDto createUser(
      UUID id,
      double lat,
      double lon,
      int radiusKm,
      boolean completed,
      FitnessLevel fitness,
      List<Activity> interests) {
    List<String> interestNames = interests.stream().map(Enum::name).toList();
    return new UserDto(
        id,
        "u@x.com",
        "User",
        fitness.name(),
        interestNames,
        lat,
        lon,
        radiusKm,
        completed,
        Instant.now());
  }
}
