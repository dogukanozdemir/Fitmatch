package com.fitmatch.user.controller;

import com.fitmatch.user.dto.CompleteProfileRequest;
import com.fitmatch.user.dto.UserDto;
import com.fitmatch.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

  private final UserService userService;

  @GetMapping("/{id}")
  public ResponseEntity<UserDto> getUser(@PathVariable("id") String id) {
    return ResponseEntity.ok(userService.getUserById(UUID.fromString(id)));
  }

  @PostMapping("/profile")
  public ResponseEntity<UserDto> getUser(@RequestBody CompleteProfileRequest completeProfileRequest) {
    return ResponseEntity.ok(userService.completeProfile(completeProfileRequest));
  }
}
