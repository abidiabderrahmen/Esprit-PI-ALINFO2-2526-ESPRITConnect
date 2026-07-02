package com.esprit.connect.repository;

import com.esprit.connect.model.CvAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CvAnalysisRepository extends JpaRepository<CvAnalysis, Long> {
}
