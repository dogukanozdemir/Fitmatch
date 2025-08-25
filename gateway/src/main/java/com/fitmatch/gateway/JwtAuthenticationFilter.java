package com.fitmatch.gateway;

import com.fitmatch.common.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

  private final JwtService jwtService;
  private final AntPathMatcher antPathMatcher = new AntPathMatcher();
  private static final String PERMITTED_ENDPOINT = "/api/auth/**";

  // TODO: better excerption handling when requests are fialing or unauth
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String endpoint = exchange.getRequest().getURI().getPath();

    if (antPathMatcher.match(PERMITTED_ENDPOINT, endpoint)) {
      return chain.filter(exchange);
    }

    String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return chain.filter(exchange);
    }

    String token = authHeader.substring(7);
    if (!jwtService.validateToken(token)) {
      return chain.filter(exchange);
    }

    String email = jwtService.getEmailFromToken(token);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(email, null, null);

    // TODO: pass the token not the email, each service validates token as well
    ServerHttpRequest mutatedRequest =
        exchange.getRequest().mutate().header("X-User-Email", email).build();
    ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

    // Proper reactive security context handling
    return chain
        .filter(mutatedExchange)
        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
  }
}
