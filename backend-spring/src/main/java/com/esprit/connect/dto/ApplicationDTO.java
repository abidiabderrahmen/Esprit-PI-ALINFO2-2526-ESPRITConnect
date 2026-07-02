package com.esprit.connect.dto;

import com.esprit.connect.model.Application;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class ApplicationDTO {

    private Long id;

    @JsonProperty("opportunity_id")
    private Long opportunityId;

    @JsonProperty("opportunity_title")
    private String opportunityTitle;

    @JsonProperty("applicant_details")
    private UserDTO applicantDetails;

    @JsonProperty("cover_letter")
    private String coverLetter;

    private String resume;
    private String status;

    @JsonProperty("applied_at")
    private LocalDateTime appliedAt;

    public ApplicationDTO() {}

    public static ApplicationDTO fromEntity(Application app) {
        ApplicationDTO dto = new ApplicationDTO();
        dto.setId(app.getId());
        dto.setOpportunityId(app.getOpportunity().getId());
        dto.setOpportunityTitle(app.getOpportunity().getTitle());
        dto.setApplicantDetails(UserDTO.fromEntity(app.getApplicant()));
        dto.setCoverLetter(app.getCoverLetter());
        dto.setResume(app.getResume());
        dto.setStatus(app.getStatus() != null ? app.getStatus().name().toLowerCase() : null);
        dto.setAppliedAt(app.getAppliedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long opportunityId) { this.opportunityId = opportunityId; }
    public String getOpportunityTitle() { return opportunityTitle; }
    public void setOpportunityTitle(String opportunityTitle) { this.opportunityTitle = opportunityTitle; }
    public UserDTO getApplicantDetails() { return applicantDetails; }
    public void setApplicantDetails(UserDTO applicantDetails) { this.applicantDetails = applicantDetails; }
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
    public String getResume() { return resume; }
    public void setResume(String resume) { this.resume = resume; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
}
