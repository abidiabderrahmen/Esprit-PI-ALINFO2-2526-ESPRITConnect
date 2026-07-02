package com.esprit.connect.repository;

import com.esprit.connect.model.Opportunity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    Page<Opportunity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
