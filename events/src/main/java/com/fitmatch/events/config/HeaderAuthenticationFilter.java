package com.fitmatch.events.config;

import com.fitmatch.common.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader != null
        && authHeader.startsWith("Bearer ")
        && SecurityContextHolder.getContext().getAuthentication() == null) {
      String jwtToken = authHeader.substring(7);
      try {
        if (jwtService.validateToken(jwtToken)) {
          String userEmail = jwtService.getEmailFromToken(jwtToken);
          String userId = jwtService.getIdFromToken(jwtToken);

          UsernamePasswordAuthenticationToken auth =
              new UsernamePasswordAuthenticationToken(userEmail, null, Collections.emptyList());

          auth.setDetails(userId);

          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      } catch (Exception e) {
        logger.warn("Failed to process JWT token: " + e.getMessage());
      }
    }

    filterChain.doFilter(request, response);
  }
}
