package com.fitmatch.user.service;

import com.fitmatch.common.dto.UserDto;
import com.fitmatch.common.enums.Activity;
import com.fitmatch.common.enums.FitnessLevel;
import com.fitmatch.user.dto.CompleteProfileRequest;
import com.fitmatch.user.entity.User;
import com.fitmatch.user.repository.UserRepository;
import com.fitmatch.user.util.GeoFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private GeoFactory geoFactory;

  @InjectMocks private UserService userService;

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void getUserById_ok() {
    UUID id = UUID.randomUUID();
    User user = new User();
    user.setId(id);
    user.setEmail("a@b.com");
    user.setFullName("Alice");
    user.setActivityInterests(Set.of(Activity.RUNNING));
    user.setFitnessLevel(FitnessLevel.BEGINNER);
    user.setSearchRadiusKm(20);
    user.setProfileCompleted(false);
    user.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));

    when(userRepository.findById(id)).thenReturn(Optional.of(user));

    UserDto dto = userService.getUserById(id);

    assertThat(dto.id()).isEqualTo(id);
    assertThat(dto.email()).isEqualTo("a@b.com");
    verify(userRepository).findById(id);
  }

  @Test
  void getUserById_notFound_throws404() {
    UUID id = UUID.randomUUID();
    when(userRepository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getUserById(id))
        .isInstanceOf(ResponseStatusException.class)
        .extracting("statusCode")
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void completeProfile_ok() {
    UUID userId = UUID.randomUUID();
    Authentication auth = mock(Authentication.class);
    when(auth.getDetails()).thenReturn(userId.toString());
    SecurityContextHolder.setContext(new SecurityContextImpl(auth));

    User user = new User();
    user.setId(userId);
    user.setEmail("a@b.com");
    user.setFullName("Alice");
    user.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    Point point = mock(Point.class);
    when(point.getY()).thenReturn(41.02);
    when(point.getX()).thenReturn(29.01);
    when(geoFactory.point(41.02, 29.01)).thenReturn(point);
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    CompleteProfileRequest req =
        new CompleteProfileRequest(
            FitnessLevel.BEGINNER, Set.of(Activity.RUNNING), 41.02, 29.01, 30);

    UserDto dto = userService.completeProfile(req);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertThat(captor.getValue().isProfileCompleted()).isTrue();
    assertThat(dto.lat()).isEqualTo(41.02);
    assertThat(dto.lon()).isEqualTo(29.01);
  }

  @Test
  void completeProfile_userNotFound_throws401() {
    UUID userId = UUID.randomUUID();
    Authentication auth = mock(Authentication.class);
    when(auth.getDetails()).thenReturn(userId.toString());
    SecurityContextHolder.setContext(new SecurityContextImpl(auth));

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    CompleteProfileRequest req =
        new CompleteProfileRequest(FitnessLevel.BEGINNER, Set.of(Activity.RUNNING), 41.0, 29.0, 10);

    assertThatThrownBy(() -> userService.completeProfile(req))
        .isInstanceOf(ResponseStatusException.class)
        .extracting("statusCode")
        .isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
