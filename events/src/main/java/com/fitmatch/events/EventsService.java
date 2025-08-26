package com.fitmatch.events;

import com.fitmatch.events.client.UserClient;
import com.fitmatch.events.client.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventsService {

  private final UserClient userClient;

  public UserDto mm() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String userId = (String) auth.getDetails();
    return userClient.getById(userId);
  }
}
