package com.fitmatch.user.service;

import com.fitmatch.user.dto.CompleteProfileRequest;
import com.fitmatch.user.dto.UserDto;
import com.fitmatch.user.entity.User;
import com.fitmatch.user.repository.UserRepository;
import com.fitmatch.user.util.GeoFactory;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final GeoFactory geoFactory;

  public UserDto getUserById(UUID id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "user with id does not exist"));

    return getUserDto(user);
  }

  // TODO: auth should have ben able to get from single source
  public UserDto completeProfile(CompleteProfileRequest completeProfileRequest) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String currentUserEmail = auth.getName();
    User user =
        userRepository
            .findByEmail(currentUserEmail)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

    user.setFitnessLevel(completeProfileRequest.fitnessLevel());
    user.setActivityInterests(completeProfileRequest.activityInterests());
    user.setLocation(
        geoFactory.point(completeProfileRequest.latitude(), completeProfileRequest.longitude()));
    user.setSearchRadiusKm(completeProfileRequest.searchRadiusKm());
    user.setProfileCompleted(true);
    return getUserDto(userRepository.save(user));
  }

  private static UserDto getUserDto(User user) {
    return UserDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .activityInterests(user.getActivityInterests())
        .fitnessLevel(user.getFitnessLevel())
        .lat(user.getLocation() != null ? user.getLocation().getY() : null)
        .lon(user.getLocation() != null ? user.getLocation().getX() : null)
        .searchRadiusKm(user.getSearchRadiusKm())
        .profileCompleted(user.isProfileCompleted())
        .createdAt(user.getCreatedAt())
        .build();
  }
}
