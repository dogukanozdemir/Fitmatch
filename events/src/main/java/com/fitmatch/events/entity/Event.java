package com.fitmatch.events.entity;

import com.fitmatch.common.enums.Activity;
import com.fitmatch.common.enums.FitnessLevel;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID organizerId;

  @Column(nullable = false)
  private String title;

  @Column(length = 1000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Activity activity;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FitnessLevel fitnessLevel;

  @Column(nullable = false)
  private LocalDateTime startsAt;

  @Column(nullable = false)
  private Integer capacity;

  private int participantCount = 0;

  @JdbcTypeCode(SqlTypes.GEOMETRY)
  @Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
  private Point location;

  @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<EventParticipant> eventParticipants = new HashSet<>();

  @CreationTimestamp private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;

  public void addParticipant(UUID userId) {
    if (eventParticipants == null) eventParticipants = new HashSet<>();

    EventParticipant eventParticipant =
        EventParticipant.builder().userId(userId).event(this).build();

    eventParticipants.add(eventParticipant);
    participantCount = eventParticipants.size();
  }
}
