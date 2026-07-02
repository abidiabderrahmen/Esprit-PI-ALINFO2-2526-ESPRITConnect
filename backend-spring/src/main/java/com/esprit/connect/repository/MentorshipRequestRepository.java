package com.esprit.connect.repository;

import com.esprit.connect.model.MentorshipRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorshipRequestRepository extends JpaRepository<MentorshipRequest, Long> {

    List<MentorshipRequest> findByMenteeIdOrMentorId(Long menteeId, Long mentorId);
}
