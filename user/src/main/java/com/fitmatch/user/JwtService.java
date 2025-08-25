package com.fitmatch.user;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtService {

  @Value("${spring.application.security.jwt-secret}")
  private String JWT_SECRET;

  private static final long EXPIRATION = 86400000L; // 1 day

  public String generateToken(String email) {
    return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
            .signWith(getSecretKey())
            .compact();
  }


  private SecretKey getSecretKey() {
    byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
