package com.esprit.connect.dto;

import com.esprit.connect.model.Comment;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class CommentDTO {

    private Long id;
    private String content;

    @JsonProperty("author_details")
    private UserDTO authorDetails;

    @JsonProperty("post_id")
    private Long postId;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public CommentDTO() {}

    public static CommentDTO fromEntity(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setAuthorDetails(UserDTO.fromEntity(comment.getAuthor()));
        dto.setPostId(comment.getPost().getId());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public UserDTO getAuthorDetails() { return authorDetails; }
    public void setAuthorDetails(UserDTO authorDetails) { this.authorDetails = authorDetails; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
