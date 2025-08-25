package com.fitmatch.user;

import com.fitmatch.common.JwtService;
import com.fitmatch.user.dto.AuthenticationResponse;
import com.fitmatch.user.dto.LoginRequest;
import com.fitmatch.user.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  public AuthenticationResponse login(LoginRequest loginRequest) {
    String email = loginRequest.email();
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, String.format("User with name %s not found", email)));
    if (!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Credentials");
    }

    String token = jwtService.generateToken(user.getId().toString(), user.getEmail());
    return AuthenticationResponse.builder().email(email).token(token).build();
  }

  public AuthenticationResponse register(RegisterRequest registerRequest) {
    String email = registerRequest.email();
    Optional<User> existingUser = userRepository.findByEmail(email);
    if (existingUser.isPresent()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Customer with this email already exists");
    }
    User user =
        User.builder()
            .email(email)
            .fullName(registerRequest.fullName())
            .passwordHash(passwordEncoder.encode(registerRequest.password()))
            .build();
    userRepository.save(user);

    String token = jwtService.generateToken(user.getId().toString(), user.getEmail());
    return AuthenticationResponse.builder().email(email).token(token).build();
  }
}
