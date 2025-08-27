package com.fitmatch.events;

import com.fitmatch.events.dto.CreateEventRequest;
import com.fitmatch.events.dto.EventDto;
import com.fitmatch.events.dto.GetNearbyEventsResponse;
import java.util.List;
import java.util.UUID;

import com.fitmatch.events.dto.JoinEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventsController {

  private final EventsService eventsService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<EventDto> createEvent(@RequestBody CreateEventRequest createEventRequest) {
    return ResponseEntity.ok(eventsService.createEvent(createEventRequest));
  }

  @GetMapping("/nearby")
  public ResponseEntity<List<GetNearbyEventsResponse>> getEvents() {
    return ResponseEntity.ok(eventsService.getNearbyEvents());
  }

  @DeleteMapping("/{eventId}")
  public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId) {
    eventsService.deleteEvent(eventId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{eventId}/join")
  public ResponseEntity<JoinEventResponse> joinEvent(@PathVariable UUID eventId) {
    return ResponseEntity.ok(eventsService.joinEvent(eventId));
  }

  @DeleteMapping("/{eventId}/leave")
  public ResponseEntity<Void> leaveEvent(@PathVariable UUID eventId) {
    eventsService.leaveEvent(eventId);
    return ResponseEntity.noContent().build();
  }
}
