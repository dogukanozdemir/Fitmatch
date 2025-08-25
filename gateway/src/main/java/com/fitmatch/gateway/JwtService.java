package com.fitmatch.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtService {

  @Value("${spring.application.security.jwt-secret}")
  private String JWT_SECRET;

  // TODO: make this a common lib

  private static final long EXPIRATION = 86400000L; // 1 day

  private Claims extractClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSecretKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  public Boolean validateToken(String token) {
    try {
      Claims claims =
          Jwts.parserBuilder()
              .setSigningKey(getSecretKey())
              .build()
              .parseClaimsJws(token)
              .getBody();
      return Boolean.TRUE;
    } catch (JwtException e) {
      log.error(e.getMessage());
      return Boolean.FALSE;
    }
  }

  public String generateToken(String email, String role) {
    return Jwts.builder()
            .setSubject(email)
            .claim("role", role)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
            .signWith(getSecretKey())
            .compact();
  }

  private boolean isTokenExpired(Claims claims) {
    return claims.getExpiration().before(new Date());
  }

  public String getEmailFromToken(String token) {
    return extractClaims(token).getSubject();
  }

  private SecretKey getSecretKey() {
    byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
