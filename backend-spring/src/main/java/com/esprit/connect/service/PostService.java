package com.esprit.connect.service;

import com.esprit.connect.model.Comment;
import com.esprit.connect.model.Like;
import com.esprit.connect.model.Post;
import com.esprit.connect.model.User;
import com.esprit.connect.repository.CommentRepository;
import com.esprit.connect.repository.LikeRepository;
import com.esprit.connect.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public PostService(PostRepository postRepository, CommentRepository commentRepository,
                       LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
    }

    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Post createPost(User author, String content, Post.PostType type) {
        Post post = new Post();
        post.setAuthor(author);
        post.setContent(content);
        post.setPostType(type != null ? type : Post.PostType.GENERAL);
        return postRepository.save(post);
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    public Post updatePost(Long id, User requester, String content) {
        Post post = getPostById(id);

        if (!post.getAuthor().getId().equals(requester.getId())) {
            throw new RuntimeException("You can only edit your own posts");
        }

        post.setContent(content);
        return postRepository.save(post);
    }

    public void deletePost(Long id, User requester) {
        Post post = getPostById(id);

        boolean isAuthor = post.getAuthor().getId().equals(requester.getId());
        boolean isStaff = Boolean.TRUE.equals(requester.getIsStaff());

        if (!isAuthor && !isStaff) {
            throw new RuntimeException("You do not have permission to delete this post");
        }

        postRepository.delete(post);
    }

    public Like likePost(Long postId, User user) {
        if (likeRepository.existsByPostIdAndUserId(postId, user.getId())) {
            throw new RuntimeException("You have already liked this post");
        }

        Post post = getPostById(postId);

        Like like = new Like();
        like.setPost(post);
        like.setUser(user);
        return likeRepository.save(like);
    }

    public void unlikePost(Long postId, User user) {
        Like like = likeRepository.findByPostIdAndUserId(postId, user.getId())
                .orElseThrow(() -> new RuntimeException("You have not liked this post"));
        likeRepository.delete(like);
    }

    public Comment addComment(Long postId, User author, String content) {
        Post post = getPostById(postId);

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setContent(content);
        return commentRepository.save(comment);
    }

    public List<Comment> getComments(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    public boolean isLikedByUser(Long postId, Long userId) {
        return likeRepository.existsByPostIdAndUserId(postId, userId);
    }
}
