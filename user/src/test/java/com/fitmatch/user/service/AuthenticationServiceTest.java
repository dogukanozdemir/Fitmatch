package com.fitmatch.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.fitmatch.common.JwtService;
import com.fitmatch.user.dto.AuthenticationResponse;
import com.fitmatch.user.dto.LoginRequest;
import com.fitmatch.user.dto.RegisterRequest;
import com.fitmatch.user.entity.User;
import com.fitmatch.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private JwtService jwtService;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private AuthenticationService authenticationService;

  private User testUser;
  private LoginRequest loginRequest;
  private RegisterRequest registerRequest;
  private final String TEST_EMAIL = "john.doe@example.com";
  private final String TEST_PASSWORD = "password123";
  private final String TEST_ENCODED_PASSWORD = "encoded-password";
  private final String TEST_FULL_NAME = "John Doe";
  private final String TEST_JWT_TOKEN = "jwt-token-123";
  private final UUID TEST_USER_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    testUser =
        User.builder()
            .id(TEST_USER_ID)
            .email(TEST_EMAIL)
            .fullName(TEST_FULL_NAME)
            .passwordHash(TEST_ENCODED_PASSWORD)
            .build();

    loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
    registerRequest = new RegisterRequest(TEST_EMAIL, TEST_FULL_NAME, TEST_PASSWORD);
  }

  @Test
  void login_withValidCredentials_shouldReturnAuthenticationResponse() {
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(TEST_PASSWORD, TEST_ENCODED_PASSWORD)).thenReturn(true);
    when(jwtService.generateToken(TEST_USER_ID.toString(), TEST_EMAIL)).thenReturn(TEST_JWT_TOKEN);

    AuthenticationResponse response = authenticationService.login(loginRequest);

    assertThat(response).isNotNull();
    assertThat(response.email()).isEqualTo(TEST_EMAIL);
    assertThat(response.token()).isEqualTo(TEST_JWT_TOKEN);

    verify(userRepository).findByEmail(TEST_EMAIL);
    verify(passwordEncoder).matches(TEST_PASSWORD, TEST_ENCODED_PASSWORD);
    verify(jwtService).generateToken(TEST_USER_ID.toString(), TEST_EMAIL);
  }

  @Test
  void login_withNonExistentUser_shouldThrowNotFoundException() {
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> authenticationService.login(loginRequest));

    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(exception.getReason()).contains("User with name " + TEST_EMAIL + " not found");

    verify(userRepository).findByEmail(TEST_EMAIL);
    verify(passwordEncoder, never()).matches(anyString(), anyString());
    verify(jwtService, never()).generateToken(anyString(), anyString());
  }

  @Test
  void login_withIncorrectPassword_shouldThrowUnauthorizedException() {
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(TEST_PASSWORD, TEST_ENCODED_PASSWORD)).thenReturn(false);

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> authenticationService.login(loginRequest));

    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(exception.getReason()).isEqualTo("Invalid Credentials");

    verify(userRepository).findByEmail(TEST_EMAIL);
    verify(passwordEncoder).matches(TEST_PASSWORD, TEST_ENCODED_PASSWORD);
    verify(jwtService, never()).generateToken(anyString(), anyString());
  }

  @Test
  void register_withValidData_shouldReturnAuthenticationResponse() {
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_ENCODED_PASSWORD);
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(jwtService.generateToken(TEST_USER_ID.toString(), TEST_EMAIL)).thenReturn(TEST_JWT_TOKEN);

    AuthenticationResponse response = authenticationService.register(registerRequest);

    assertThat(response).isNotNull();
    assertThat(response.email()).isEqualTo(TEST_EMAIL);
    assertThat(response.token()).isEqualTo(TEST_JWT_TOKEN);

    verify(userRepository).findByEmail(TEST_EMAIL);
    verify(passwordEncoder).encode(TEST_PASSWORD);
    verify(userRepository)
        .save(
            argThat(
                user ->
                    user.getEmail().equals(TEST_EMAIL)
                        && user.getFullName().equals(TEST_FULL_NAME)
                        && user.getPasswordHash().equals(TEST_ENCODED_PASSWORD)));
    verify(jwtService).generateToken(TEST_USER_ID.toString(), TEST_EMAIL);
  }

  @Test
  void register_withExistingUser_shouldThrowBadRequestException() {
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> authenticationService.register(registerRequest));

    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(exception.getReason()).isEqualTo("Customer with this email already exists");

    verify(userRepository).findByEmail(TEST_EMAIL);
    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any(User.class));
    verify(jwtService, never()).generateToken(anyString(), anyString());
  }

  @Test
  void register_shouldCreateUserWithCorrectData() {
    when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_ENCODED_PASSWORD);
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(jwtService.generateToken(any(), any())).thenReturn(TEST_JWT_TOKEN);

    authenticationService.register(registerRequest);

    verify(userRepository)
        .save(
            argThat(
                user -> {
                  assertThat(user.getEmail()).isEqualTo(TEST_EMAIL);
                  assertThat(user.getFullName()).isEqualTo(TEST_FULL_NAME);
                  assertThat(user.getPasswordHash()).isEqualTo(TEST_ENCODED_PASSWORD);
                  return true;
                }));
  }
}
