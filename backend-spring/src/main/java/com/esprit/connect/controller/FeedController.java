package com.esprit.connect.controller;

import com.esprit.connect.dto.CommentDTO;
import com.esprit.connect.dto.PaginatedResponse;
import com.esprit.connect.dto.PostDTO;
import com.esprit.connect.model.Comment;
import com.esprit.connect.model.Like;
import com.esprit.connect.model.Post;
import com.esprit.connect.model.User;
import com.esprit.connect.repository.CommentRepository;
import com.esprit.connect.repository.LikeRepository;
import com.esprit.connect.repository.PostRepository;
import com.esprit.connect.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class FeedController {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    public FeedController(PostRepository postRepository, CommentRepository commentRepository,
                          LikeRepository likeRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/feed/")
    public ResponseEntity<?> listPosts(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int page_size) {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page - 1, page_size);
        Page<Post> postPage = postRepository.findAllByOrderByCreatedAtDesc(pageable);

        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        List<PostDTO> postDTOs = postPage.getContent().stream()
                .map(post -> PostDTO.fromEntity(post, currentUserId))
                .collect(Collectors.toList());

        PaginatedResponse<PostDTO> response = PaginatedResponse.fromPage(postPage, postDTOs);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/feed/")
    public ResponseEntity<?> createPost(@RequestBody Map<String, String> request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        String content = request.get("content");
        String postType = request.get("post_type");

        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("content", "Content is required."));
        }

        Post post = new Post();
        post.setAuthor(currentUser);
        post.setContent(content);
        if (postType != null) {
            try {
                post.setPostType(Post.PostType.valueOf(postType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                post.setPostType(Post.PostType.GENERAL);
            }
        }

        Post saved = postRepository.save(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(PostDTO.fromEntity(saved, currentUser.getId()));
    }

    @GetMapping("/feed/{id}/")
    public ResponseEntity<?> getPost(@PathVariable Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "Post not found."));
        }

        User currentUser = getCurrentUser();
        Long uid = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(PostDTO.fromEntity(post, uid));
    }

    @PutMapping("/feed/{id}/")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody Map<String, String> request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "Post not found."));
        }
        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("detail", "You can only edit your own posts."));
        }

        if (request.containsKey("content")) post.setContent(request.get("content"));
        if (request.containsKey("post_type")) {
            try {
                post.setPostType(Post.PostType.valueOf(request.get("post_type").toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }

        Post saved = postRepository.save(post);
        return ResponseEntity.ok(PostDTO.fromEntity(saved, currentUser.getId()));
    }

    @DeleteMapping("/feed/{id}/")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "Post not found."));
        }
        if (!post.getAuthor().getId().equals(currentUser.getId()) && !Boolean.TRUE.equals(currentUser.getIsStaff())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("detail", "You do not have permission to delete this post."));
        }

        postRepository.delete(post);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/feed/{id}/like/")
    public ResponseEntity<?> likePost(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "Post not found."));
        }

        if (likeRepository.existsByPostIdAndUserId(id, currentUser.getId())) {
            return ResponseEntity.badRequest().body(Map.of("detail", "You already liked this post."));
        }

        Like like = new Like();
        like.setPost(post);
        like.setUser(currentUser);
        likeRepository.save(like);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("detail", "Post liked."));
    }

    @PostMapping("/feed/{id}/unlike/")
    public ResponseEntity<?> unlikePost(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        Like like = likeRepository.findByPostIdAndUserId(id, currentUser.getId()).orElse(null);
        if (like == null) {
            return ResponseEntity.badRequest().body(Map.of("detail", "You have not liked this post."));
        }

        likeRepository.delete(like);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/feed/{id}/comment/")
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody Map<String, String> request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("detail", "Authentication required."));
        }

        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "Post not found."));
        }

        String content = request.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("content", "Content is required."));
        }

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(currentUser);
        comment.setContent(content);
        Comment saved = commentRepository.save(comment);

        return ResponseEntity.status(HttpStatus.CREATED).body(CommentDTO.fromEntity(saved));
    }

    @GetMapping("/feed/{id}/get_comments/")
    public ResponseEntity<?> getComments(@PathVariable Long id) {
        if (!postRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("detail", "Post not found."));
        }

        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(id);
        List<CommentDTO> commentDTOs = comments.stream()
                .map(CommentDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(commentDTOs);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        String username;
        if (auth.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) auth.getPrincipal()).getUsername();
        } else {
            username = auth.getPrincipal().toString();
        }
        return userRepository.findByUsername(username).orElse(null);
    }
}
