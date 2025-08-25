package com.fitmatch.user.entity;

import com.fitmatch.user.enums.Activity;
import com.fitmatch.user.enums.FitnessLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "full_name", nullable = false, length = 100)
  private String fullName;

  @Column(name = "fitness_level")
  @Enumerated(EnumType.STRING)
  private FitnessLevel fitnessLevel;

  @Column(name = "activity_interests")
  @ElementCollection
  private List<Activity> activityInterests;

  @JdbcTypeCode(SqlTypes.GEOMETRY)
  @Column(name = "location", columnDefinition = "geometry(Point,4326)")
  private Point location;

  @Column(name = "search_radius_km")
  private Integer searchRadiusKm = 10;

  @Column(name = "profile_completed")
  private boolean profileCompleted = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
