package com.esprit.connect.dto;

import com.esprit.connect.model.MentorshipRequest;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class MentorshipRequestDTO {

    private Long id;

    @JsonProperty("mentee_details")
    private UserDTO menteeDetails;

    @JsonProperty("mentor_details")
    private UserDTO mentorDetails;

    private String message;
    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public MentorshipRequestDTO() {}

    public static MentorshipRequestDTO fromEntity(MentorshipRequest req) {
        MentorshipRequestDTO dto = new MentorshipRequestDTO();
        dto.setId(req.getId());
        dto.setMenteeDetails(UserDTO.fromEntity(req.getMentee()));
        dto.setMentorDetails(UserDTO.fromEntity(req.getMentor()));
        dto.setMessage(req.getMessage());
        dto.setStatus(req.getStatus() != null ? req.getStatus().name().toLowerCase() : null);
        dto.setCreatedAt(req.getCreatedAt());
        dto.setUpdatedAt(req.getUpdatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserDTO getMenteeDetails() { return menteeDetails; }
    public void setMenteeDetails(UserDTO menteeDetails) { this.menteeDetails = menteeDetails; }
    public UserDTO getMentorDetails() { return mentorDetails; }
    public void setMentorDetails(UserDTO mentorDetails) { this.mentorDetails = mentorDetails; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
