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
      String jwtToken = authHeader.substring(7); // Remove "Bearer " prefix
      try {
        // Validate the token first
        if (jwtService.validateToken(jwtToken)) {
          String userEmail = jwtService.getEmailFromToken(jwtToken);
          String userId = jwtService.getIdFromToken(jwtToken);

          // You can use either email or userId as the principal depending on your needs
          // Using email here to maintain compatibility with existing code
          UsernamePasswordAuthenticationToken auth =
              new UsernamePasswordAuthenticationToken(userEmail, null, Collections.emptyList());

          // Optionally, you can add the userId as a detail if needed elsewhere
          auth.setDetails(userId);

          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      } catch (Exception e) {
        // Log the error if needed, but don't break the filter chain
        // The request will proceed without authentication
        logger.warn("Failed to process JWT token: " + e.getMessage());
      }
    }

    filterChain.doFilter(request, response);
  }
}
