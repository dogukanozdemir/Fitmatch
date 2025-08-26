package com.fitmatch.events;

import com.fitmatch.events.client.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventsController {

  private final EventsService eventsService;

  @PostMapping
  public ResponseEntity<UserDto> getEvents() {
    return ResponseEntity.ok(eventsService.mm());
  }
}
