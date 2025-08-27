package com.fitmatch.events;

import com.fitmatch.common.enums.Activity;
import com.fitmatch.common.enums.ActivityCategory;
import com.fitmatch.common.enums.FitnessLevel;
import com.fitmatch.events.client.UserClient;
import com.fitmatch.events.client.dto.UserDto;
import com.fitmatch.events.dto.*;
import com.fitmatch.events.entity.Event;
import com.fitmatch.events.entity.EventParticipant;
import com.fitmatch.events.repository.EventParticipantRepository;
import com.fitmatch.events.repository.EventsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EventsService {

  private static final double W_GEO = 0.40;
  private static final double W_ACTIVITY = 0.40;
  private static final double W_FITNESS = 0.20;

  private final UserClient userClient;
  private final EventsRepository eventsRepository;
  private final GeometryFactory geometryFactory;
  private final EventParticipantRepository eventParticipantRepository;

  private final EntityManager entityManager;

  @Transactional
  public EventDto createEvent(CreateEventRequest createEventRequest) {
    Point point =
        geometryFactory.createPoint(
            new Coordinate(createEventRequest.lng(), createEventRequest.lat()));
    point.setSRID(4326);

    UserDto currentUser = getCurrentUser();

    Event event =
        Event.builder()
            .organizerId(currentUser.id())
            .title(createEventRequest.title())
            .description(createEventRequest.description())
            .activity(createEventRequest.activityType())
            .fitnessLevel(createEventRequest.fitnessLevel())
            .startsAt(createEventRequest.startsAt())
            .capacity(createEventRequest.capacity())
            .location(point)
            .participantCount(0)
            .build();

    event.addParticipant(currentUser.id());
    event.setParticipantCount(1);

    Event saved = eventsRepository.save(event);
    return EventDto.builder()
        .eventId(saved.getId())
        .title(saved.getTitle())
        .description(saved.getDescription())
        .activity(saved.getActivity())
        .fitnessLevel(saved.getFitnessLevel())
        .startsAt(saved.getStartsAt())
        .capacity(saved.getCapacity())
        .participantCount(saved.getParticipantCount())
        .build();
  }

  @Transactional
  public JoinEventResponse joinEvent(UUID eventId) {
    UserDto currentUser = getCurrentUser();
    // Use pessimistic lock to prevent race conditions
    Event event = entityManager.find(Event.class, eventId, LockModeType.PESSIMISTIC_WRITE);
    if (event == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
    }

    boolean alreadyAttending =
        eventParticipantRepository.existsByEventIdAndUserId(eventId, currentUser.id());
    if (alreadyAttending) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "User is already attending this event");
    }

    if (event.getStartsAt().isBefore(java.time.LocalDateTime.now())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot join an event that has already started");
    }

    // Check capacity AFTER acquiring the lock to ensure accuracy
    if (event.getParticipantCount() >= event.getCapacity()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event is at full capacity");
    }

    try {
      EventParticipant participant =
          EventParticipant.builder().userId(currentUser.id()).event(event).build();

      eventParticipantRepository.save(participant);

      // Only increment count after successful participant creation
      event.setParticipantCount(event.getParticipantCount() + 1);
      eventsRepository.save(event);

    } catch (Exception e) {
      // If anything fails, the transaction will be rolled back automatically
      // Log the error for debugging
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to join event. Please try again.");
    }
    return JoinEventResponse.builder()
        .message("Successfully joined the event!")
        .event(
            EventDto.builder()
                .eventId(eventId)
                .title(event.getTitle())
                .description(event.getDescription())
                .activity(event.getActivity())
                .fitnessLevel(event.getFitnessLevel())
                .startsAt(event.getStartsAt())
                .capacity(event.getCapacity())
                .participantCount(event.getParticipantCount())
                .build())
        .build();
  }

  @Transactional
  public void deleteEvent(UUID eventId) {
    Event event =
        eventsRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

    UserDto currentUser = getCurrentUser();
    if (!event.getOrganizerId().equals(currentUser.id())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Only the event organizer can delete this event");
    }

    eventsRepository.delete(event);
  }

  @Transactional
  public void leaveEvent(UUID eventId) {
    Event event =
        eventsRepository
            .findById(eventId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

    UserDto currentUser = getCurrentUser();
    if (currentUser.id().equals(event.getOrganizerId())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "You can't leave your own event, please delete");
    }
    EventParticipant attendee =
        eventParticipantRepository
            .findByEventIdAndUserId(eventId, currentUser.id())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User is not attending this event"));

    eventParticipantRepository.delete(attendee);

    event.setParticipantCount(Math.max(0, event.getParticipantCount() - 1));
    eventsRepository.save(event);
  }

  public List<GetNearbyEventsResponse> getNearbyEvents() {
    UserDto user = getCurrentUser();
    if (user == null || !user.profileCompleted()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User profile not completed");
    }

    double radiusMeters = user.searchRadiusKm() * 1000.0;

    FitnessLevel userFitness = FitnessLevel.valueOf(user.fitnessLevel());
    List<Activity> userInterestEnums =
        user.activityInterests().stream().map(Activity::valueOf).toList();

    List<NearbyEventView> nearbyEvents =
        eventsRepository.findNearbyEvents(user.lat(), user.lon(), radiusMeters);

    return nearbyEvents.stream()
        .map(
            event ->
                NearbyEventDto.builder()
                    .id(event.getId())
                    .title(event.getTitle())
                    .activity(event.getActivity())
                    .fitnessLevel(event.getFitnessLevel())
                    .startsAt(event.getStartsAt())
                    .capacity(event.getCapacity())
                    .participantCount(event.getParticipantCount())
                    .distance(event.getDistance())
                    .lat(event.getLat())
                    .lng(event.getLng())
                    .build())
        .map(
            event -> {
              double score = computeScore(event, userFitness, userInterestEnums, radiusMeters);
              return GetNearbyEventsResponse.builder()
                  .compatibilityScore(Math.max(0.0, Math.min(100.0, score)))
                  .event(event)
                  .build();
            })
        .sorted(
            Comparator.<GetNearbyEventsResponse>comparingDouble(
                    response -> -response.compatibilityScore())
                .thenComparing(
                    (GetNearbyEventsResponse response) ->
                        response.event().getParticipantCount() == null
                            ? 0
                            : response.event().getParticipantCount(),
                    Comparator.reverseOrder())
                .thenComparingDouble(response -> response.event().getDistance()))
        .toList();
  }

  private static double computeScore(
      NearbyEventDto ev,
      FitnessLevel userFitness,
      List<Activity> userInterestEnums,
      double radiusMeters) {
    double geoCloseness = 1.0 - Math.min(ev.getDistance() / radiusMeters, 1.0);

    Activity eventActivity = Activity.valueOf(ev.getActivity());
    double activityAffinity = bestActivityAffinity(eventActivity, userInterestEnums);

    FitnessLevel eventFitness = FitnessLevel.valueOf(ev.getFitnessLevel());
    double fitnessAffinity = fitnessAffinity(eventFitness, userFitness);

    double combined =
        W_GEO * geoCloseness + W_ACTIVITY * activityAffinity + W_FITNESS * fitnessAffinity;

    return 100.0 * combined;
  }

  private static double bestActivityAffinity(Activity event, List<Activity> userActivities) {
    if (userActivities.isEmpty()) return 0.0;

    if (userActivities.contains(event)) return 1.0;

    ActivityCategory eventCategory = event.category();
    for (Activity activity : userActivities) {
      if (activity.category() == eventCategory) return 0.5;
    }
    return 0.0;
  }

  private static double fitnessAffinity(FitnessLevel eventFitnessLevel, FitnessLevel user) {
    int distance = Math.abs(rank(eventFitnessLevel) - rank(user));
    return switch (distance) {
      case 0 -> 1.0;
      case 1 -> 0.5;
      default -> 0.0;
    };
  }

  private static int rank(FitnessLevel fitnessLevel) {
    return switch (fitnessLevel) {
      case BEGINNER -> 0;
      case INTERMEDIATE -> 1;
      case ADVANCED -> 2;
    };
  }

  private UserDto getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getDetails() == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authentication found");
    }
    String userId = (String) auth.getDetails();
    UserDto user = userClient.getById(userId);
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }
    return user;
  }
}
