package com.fitmatch.events.repository;

import com.fitmatch.events.entity.EventParticipant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventParticipantRepository extends JpaRepository<EventParticipant, UUID> {

  Optional<EventParticipant> findByEventIdAndUserId(UUID id, UUID userId);

  boolean existsByEventIdAndUserId(UUID id, UUID userId);
}
