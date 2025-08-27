package com.fitmatch.events.repository;

import com.fitmatch.events.dto.NearbyEventView;
import com.fitmatch.events.entity.Event;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventsRepository extends JpaRepository<Event, UUID> {

  @Query(
      value =
          """
      SELECT e.id,
             e.title,
             e.activity        AS activity,
             e.fitness_level   AS fitnessLevel,
             e.starts_at       AS startsAt,
             e.capacity        AS capacity,
             e.participant_count  AS participantCount,
             ST_Distance(
               e.location::geography,
               ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
             )                 AS distance,
             ST_Y(e.location)  AS lat,
             ST_X(e.location)  AS lng
      FROM events e
      WHERE ST_DWithin(
              e.location::geography,
              ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
              :radiusMeters
            )
      ORDER BY distance ASC
      """,
      nativeQuery = true)
  List<NearbyEventView> findNearbyEvents(
      @Param("lat") double lat,
      @Param("lng") double lng,
      @Param("radiusMeters") double radiusMeters);
}
