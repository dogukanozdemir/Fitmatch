package com.fitmatch.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitmatch.user.dto.AuthenticationResponse;
import com.fitmatch.user.dto.LoginRequest;
import com.fitmatch.user.dto.RegisterRequest;
import com.fitmatch.user.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

  private MockMvc mockMvc;

  @Mock private AuthenticationService authenticationService;

  @InjectMocks private AuthenticationController controller;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setValidator(new LocalValidatorFactoryBean())
            .build();
  }

  @Test
  void login_ok() throws Exception {
    AuthenticationResponse resp =
        AuthenticationResponse.builder().email("a@b.com").token("jwt-token").build();

    when(authenticationService.login(any(LoginRequest.class))).thenReturn(resp);

    String body = objectMapper.writeValueAsString(new LoginRequest("a@b.com", "secret123"));

    mockMvc
        .perform(post("/api/auth/login").contentType(APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
        .andExpect(jsonPath("$.email").value("a@b.com"))
        .andExpect(jsonPath("$.token").value("jwt-token"));

    verify(authenticationService)
        .login(argThat(lr -> lr.email().equals("a@b.com") && lr.password().equals("secret123")));
  }

  @Test
  void login_invalidCredentials() throws Exception {
    when(authenticationService.login(any(LoginRequest.class)))
        .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Credentials"));

    String body = objectMapper.writeValueAsString(new LoginRequest("a@b.com", "badpass"));

    mockMvc
        .perform(post("/api/auth/login").contentType(APPLICATION_JSON).content(body))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void login_validationError() throws Exception {
    String body = """
      { "email": "", "password": "secret123" }
      """;

    mockMvc
        .perform(post("/api/auth/login").contentType(APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(authenticationService);
  }

  @Test
  void register_created() throws Exception {
    AuthenticationResponse resp =
        AuthenticationResponse.builder().email("new@user.com").token("new-jwt").build();

    when(authenticationService.register(any(RegisterRequest.class))).thenReturn(resp);

    String body =
        objectMapper.writeValueAsString(
            new RegisterRequest("new@user.com", "New User", "p@ssw0rd"));

    mockMvc
        .perform(post("/api/auth/register").contentType(APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
        .andExpect(jsonPath("$.email").value("new@user.com"))
        .andExpect(jsonPath("$.token").value("new-jwt"));

    verify(authenticationService)
        .register(
            argThat(
                rr ->
                    rr.email().equals("new@user.com")
                        && rr.fullName().equals("New User")
                        && rr.password().equals("p@ssw0rd")));
  }

  @Test
  void register_emailExists() throws Exception {
    when(authenticationService.register(any(RegisterRequest.class)))
        .thenThrow(
            new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Customer with this email already exists"));

    String body =
        objectMapper.writeValueAsString(
            new RegisterRequest("dup@user.com", "Dup User", "password"));

    mockMvc
        .perform(post("/api/auth/register").contentType(APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void register_validationError() throws Exception {
    // Missing fullName
    String body = """
      { "email": "x@y.com", "password": "secret123" }
      """;

    mockMvc
        .perform(post("/api/auth/register").contentType(APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(authenticationService);
  }
}
