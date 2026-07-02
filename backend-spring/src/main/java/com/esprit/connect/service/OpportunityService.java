package com.esprit.connect.service;

import com.esprit.connect.model.Application;
import com.esprit.connect.model.Opportunity;
import com.esprit.connect.model.User;
import com.esprit.connect.repository.ApplicationRepository;
import com.esprit.connect.repository.OpportunityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final ApplicationRepository applicationRepository;

    public OpportunityService(OpportunityRepository opportunityRepository,
                              ApplicationRepository applicationRepository) {
        this.opportunityRepository = opportunityRepository;
        this.applicationRepository = applicationRepository;
    }

    public Page<Opportunity> getAllOpportunities(Pageable pageable) {
        return opportunityRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Opportunity createOpportunity(User postedBy, Opportunity opportunity) {
        opportunity.setPostedBy(postedBy);
        return opportunityRepository.save(opportunity);
    }

    public Opportunity getOpportunityById(Long id) {
        return opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found with id: " + id));
    }

    public Application applyToOpportunity(User applicant, Long opportunityId, String coverLetter) {
        Opportunity opportunity = getOpportunityById(opportunityId);

        if (applicationRepository.existsByOpportunityIdAndApplicantId(opportunityId, applicant.getId())) {
            throw new RuntimeException("You have already applied to this opportunity");
        }

        Application application = new Application();
        application.setOpportunity(opportunity);
        application.setApplicant(applicant);
        application.setCoverLetter(coverLetter);
        return applicationRepository.save(application);
    }

    public List<Application> getApplicationsByUser(Long userId) {
        return applicationRepository.findByApplicantId(userId);
    }

    public long getApplicationsCount(Long opportunityId) {
        return applicationRepository.countByOpportunityId(opportunityId);
    }
}
