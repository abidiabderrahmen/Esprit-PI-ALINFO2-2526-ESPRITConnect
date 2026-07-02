package com.esprit.connect.dto;

import com.esprit.connect.model.Post;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PostDTO {

    private Long id;
    private Long author;

    @JsonProperty("author_details")
    private UserDTO authorDetails;

    private String content;
    private String image;

    @JsonProperty("post_type")
    private String postType;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("likes_count")
    private int likesCount;

    @JsonProperty("comments_count")
    private int commentsCount;

    private List<CommentDTO> comments;

    @JsonProperty("is_liked")
    private boolean isLiked;

    public PostDTO() {}

    public static PostDTO fromEntity(Post post, Long currentUserId) {
        if (post == null) return null;
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setAuthor(post.getAuthor() != null ? post.getAuthor().getId() : null);
        dto.setAuthorDetails(UserDTO.fromEntity(post.getAuthor()));
        dto.setContent(post.getContent());
        dto.setImage(post.getImage());
        dto.setPostType(post.getPostType() != null ? post.getPostType().name() : "GENERAL");
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setLikesCount(post.getLikes() != null ? post.getLikes().size() : 0);
        dto.setCommentsCount(post.getComments() != null ? post.getComments().size() : 0);

        if (post.getComments() != null) {
            dto.setComments(post.getComments().stream()
                    .map(CommentDTO::fromEntity)
                    .collect(Collectors.toList()));
        }

        if (currentUserId != null && post.getLikes() != null) {
            dto.setLiked(post.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(currentUserId)));
        } else {
            dto.setLiked(false);
        }

        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAuthor() { return author; }
    public void setAuthor(Long author) { this.author = author; }

    public UserDTO getAuthorDetails() { return authorDetails; }
    public void setAuthorDetails(UserDTO authorDetails) { this.authorDetails = authorDetails; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getPostType() { return postType; }
    public void setPostType(String postType) { this.postType = postType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }

    public int getCommentsCount() { return commentsCount; }
    public void setCommentsCount(int commentsCount) { this.commentsCount = commentsCount; }

    public List<CommentDTO> getComments() { return comments; }
    public void setComments(List<CommentDTO> comments) { this.comments = comments; }

    public boolean isLiked() { return isLiked; }
    public void setLiked(boolean liked) { isLiked = liked; }
}
