package com.fitmatch.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UseController {

  private final AuthenticationUtil authenticationUtil;

  @GetMapping
  public ResponseEntity<String> getUser() {
    return ResponseEntity.ok(authenticationUtil.getAuthenticatedCustomer().getEmail());
  }
}
