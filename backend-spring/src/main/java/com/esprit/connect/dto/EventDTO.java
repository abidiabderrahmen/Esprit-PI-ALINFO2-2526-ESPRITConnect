package com.esprit.connect.dto;

import com.esprit.connect.model.Event;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EventDTO {

    private Long id;
    private String title;
    private String description;

    @JsonProperty("event_type")
    private String eventType;

    private String location;
    private LocalDate date;
    private LocalTime time;

    @JsonProperty("max_participants")
    private Integer maxParticipants;

    private String image;

    @JsonProperty("is_online")
    private Boolean isOnline;

    @JsonProperty("meeting_link")
    private String meetingLink;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("organizer_details")
    private UserDTO organizerDetails;

    @JsonProperty("registered_count")
    private int registeredCount;

    @JsonProperty("is_full")
    private boolean isFull;

    @JsonProperty("is_registered")
    private boolean isRegistered;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public EventDTO() {}

    public static EventDTO fromEntity(Event event, boolean registered) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setEventType(event.getEventType() != null ? event.getEventType().name().toLowerCase() : null);
        dto.setLocation(event.getLocation());
        dto.setDate(event.getDate());
        dto.setTime(event.getTime());
        dto.setMaxParticipants(event.getMaxParticipants());
        dto.setImage(event.getImage());
        dto.setIsOnline(event.getIsOnline());
        dto.setMeetingLink(event.getMeetingLink());
        dto.setIsActive(event.getIsActive());
        dto.setOrganizerDetails(UserDTO.fromEntity(event.getOrganizer()));
        dto.setRegisteredCount(event.getRegisteredCount());
        dto.setFull(event.isFull());
        dto.setRegistered(registered);
        dto.setCreatedAt(event.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }
    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Boolean getIsOnline() { return isOnline; }
    public void setIsOnline(Boolean isOnline) { this.isOnline = isOnline; }
    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public UserDTO getOrganizerDetails() { return organizerDetails; }
    public void setOrganizerDetails(UserDTO organizerDetails) { this.organizerDetails = organizerDetails; }
    public int getRegisteredCount() { return registeredCount; }
    public void setRegisteredCount(int registeredCount) { this.registeredCount = registeredCount; }
    public boolean isFull() { return isFull; }
    public void setFull(boolean full) { isFull = full; }
    public boolean isRegistered() { return isRegistered; }
    public void setRegistered(boolean registered) { isRegistered = registered; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
