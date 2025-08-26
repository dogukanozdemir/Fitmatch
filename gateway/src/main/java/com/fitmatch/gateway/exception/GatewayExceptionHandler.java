package com.fitmatch.gateway.exception;

import io.jsonwebtoken.JwtException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
@RestControllerAdvice
public class GatewayExceptionHandler {

  @ExceptionHandler(value = Exception.class)
  public ResponseEntity<Object> defaultErrorHandler(Exception e) {
    return new ResponseEntity<>(
        ErrorResponse.builder().timestamp(LocalDateTime.now()).message(e.getMessage()).build(),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(value = MethodArgumentNotValidException.class)
  public ResponseEntity<Object> methodArgumentNotValidHandler(MethodArgumentNotValidException e) {
    Map<String, String> errorMap = new HashMap<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errorMap.put(error.getField(), error.getDefaultMessage()));
    return new ResponseEntity<>(
        ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .message("Constraint Validation Failed")
            .errors(errorMap)
            .build(),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(value = AuthenticationCredentialsNotFoundException.class)
  public ResponseEntity<Object> missingCredentialsHandler(
      AuthenticationCredentialsNotFoundException e) {
    return new ResponseEntity<>(
        ErrorResponse.builder()
            .message(
                "Authentication required. Please provide a valid Bearer token in the Authorization header.")
            .timestamp(LocalDateTime.now())
            .build(),
        HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(value = BadCredentialsException.class)
  public ResponseEntity<Object> badCredentialsHandler(BadCredentialsException e) {
    return new ResponseEntity<>(
        ErrorResponse.builder()
            .message("Invalid or expired token. Please authenticate again.")
            .timestamp(LocalDateTime.now())
            .build(),
        HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(value = JwtException.class)
  public ResponseEntity<Object> jwtExceptionHandler(JwtException e) {
    return new ResponseEntity<>(
        ErrorResponse.builder()
            .message("Token validation failed: " + e.getMessage())
            .timestamp(LocalDateTime.now())
            .build(),
        HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(
      value = {AuthenticationException.class, InsufficientAuthenticationException.class})
  public ResponseEntity<Object> authenticationExceptionHandler(AuthenticationException e) {
    return new ResponseEntity<>(
        ErrorResponse.builder()
            .message("Authentication failed: " + e.getMessage())
            .timestamp(LocalDateTime.now())
            .build(),
        HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(value = ResponseStatusException.class)
  public ResponseEntity<Object> responseStatusExceptionHandler(ResponseStatusException ex) {
    return new ResponseEntity<>(
        ErrorResponse.builder().message(ex.getReason()).timestamp(LocalDateTime.now()).build(),
        ex.getStatusCode());
  }
}
