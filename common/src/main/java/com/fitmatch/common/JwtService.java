package com.fitmatch.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtService {

  @Value("${spring.application.security.jwt-secret}")
  private String JWT_SECRET;

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
      return !isTokenExpired(claims);
    } catch (JwtException e) {
      return Boolean.FALSE;
    }
  }

  public String generateToken(String id, String email) {
    return Jwts.builder()
        .setSubject(id)
        .claim("email", email)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
        .signWith(getSecretKey())
        .compact();
  }

  private SecretKey getSecretKey() {
    byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  private boolean isTokenExpired(Claims claims) {
    return claims.getExpiration().before(new Date());
  }

  public String getEmailFromToken(String token) {
    return extractClaims(token).get("email", String.class);
  }

  public String getIdFromToken(String token) {
    return extractClaims(token).getSubject();
  }
}
