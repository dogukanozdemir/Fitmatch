package com.fitmatch.user.entity;

import com.fitmatch.common.enums.Activity;
import com.fitmatch.common.enums.FitnessLevel;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

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

  @ElementCollection(targetClass = Activity.class, fetch = FetchType.LAZY)
  @CollectionTable(name = "user_activity_interests", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "activity", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private Set<Activity> activityInterests; // or Set<Activity> to prevent duplicates

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
