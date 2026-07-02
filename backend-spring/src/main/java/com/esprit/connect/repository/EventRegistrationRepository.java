package com.esprit.connect.repository;

import com.esprit.connect.model.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    Optional<EventRegistration> findByEventIdAndParticipantId(Long eventId, Long participantId);

    boolean existsByEventIdAndParticipantId(Long eventId, Long participantId);
}
