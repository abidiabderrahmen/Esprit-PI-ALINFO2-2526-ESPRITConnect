package com.esprit.connect.dto;

import com.esprit.connect.model.Opportunity;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class OpportunityDTO {

    private Long id;
    private String title;
    private String description;

    @JsonProperty("opportunity_type")
    private String opportunityType;

    @JsonProperty("company_name")
    private String companyName;

    private String location;

    @JsonProperty("salary_range")
    private String salaryRange;

    private String requirements;

    @JsonProperty("is_remote")
    private Boolean isRemote;

    private LocalDate deadline;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("posted_by_details")
    private UserDTO postedByDetails;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public OpportunityDTO() {}

    public static OpportunityDTO fromEntity(Opportunity opp) {
        OpportunityDTO dto = new OpportunityDTO();
        dto.setId(opp.getId());
        dto.setTitle(opp.getTitle());
        dto.setDescription(opp.getDescription());
        dto.setOpportunityType(opp.getOpportunityType() != null ? opp.getOpportunityType().name().toLowerCase() : null);
        dto.setCompanyName(opp.getCompanyName());
        dto.setLocation(opp.getLocation());
        dto.setSalaryRange(opp.getSalaryRange());
        dto.setRequirements(opp.getRequirements());
        dto.setIsRemote(opp.getIsRemote());
        dto.setDeadline(opp.getDeadline());
        dto.setIsActive(opp.getIsActive());
        dto.setPostedByDetails(UserDTO.fromEntity(opp.getPostedBy()));
        dto.setCreatedAt(opp.getCreatedAt());
        dto.setUpdatedAt(opp.getUpdatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOpportunityType() { return opportunityType; }
    public void setOpportunityType(String opportunityType) { this.opportunityType = opportunityType; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getSalaryRange() { return salaryRange; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public Boolean getIsRemote() { return isRemote; }
    public void setIsRemote(Boolean isRemote) { this.isRemote = isRemote; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public UserDTO getPostedByDetails() { return postedByDetails; }
    public void setPostedByDetails(UserDTO postedByDetails) { this.postedByDetails = postedByDetails; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
