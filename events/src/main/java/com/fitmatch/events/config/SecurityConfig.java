package com.fitmatch.events.config;

import com.fitmatch.common.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http, HeaderAuthenticationFilter headerAuthFilter) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public HeaderAuthenticationFilter headerAuthenticationFilter(JwtService jwtService) {
    return new HeaderAuthenticationFilter(jwtService);
  }
}
