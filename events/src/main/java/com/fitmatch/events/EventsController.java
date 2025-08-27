package com.fitmatch.events;

import com.fitmatch.events.dto.CreateEventRequest;
import com.fitmatch.events.dto.GetNearbyEventsResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventsController {

  private final EventsService eventsService;

  @PostMapping
  public ResponseEntity<String> createEvent(@RequestBody CreateEventRequest createEventRequest) {
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
  public ResponseEntity<String> joinEvent(@PathVariable UUID eventId) {
    eventsService.joinEvent(eventId);
    return ResponseEntity.ok("Successfully joined the event");
  }

  @DeleteMapping("/{eventId}/leave")
  public ResponseEntity<String> leaveEvent(@PathVariable UUID eventId) {
    eventsService.leaveEvent(eventId);
    return ResponseEntity.ok("Successfully left the event");
  }
}
