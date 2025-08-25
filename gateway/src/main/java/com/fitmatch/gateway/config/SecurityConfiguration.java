package com.fitmatch.gateway.config;

import com.fitmatch.gateway.JwtAuthenticationFilter;
import com.fitmatch.gateway.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

  private static final String PERMITTED_ENDPOINT = "/api/auth/**";

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(
      ServerHttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) {
    return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(
            exchange ->
                exchange.pathMatchers(PERMITTED_ENDPOINT).permitAll().anyExchange().authenticated())
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
        .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .build();
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
    return new JwtAuthenticationFilter(jwtService);
  }
}
