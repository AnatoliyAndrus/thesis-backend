package com.naukma.thesisbackend.controllers;

import com.naukma.thesisbackend.dtos.CommentDto;
import com.naukma.thesisbackend.dtos.CommentRequestDto;
import com.naukma.thesisbackend.dtos.PostDto;
import com.naukma.thesisbackend.dtos.PostRequestDto;
import com.naukma.thesisbackend.entities.Comment;
import com.naukma.thesisbackend.entities.Post;
import com.naukma.thesisbackend.entities.User;
import com.naukma.thesisbackend.exceptions.ForbiddenException;
import com.naukma.thesisbackend.services.AuthService;
import com.naukma.thesisbackend.services.CommentService;
import com.naukma.thesisbackend.services.PostService;
import com.naukma.thesisbackend.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("api/v1/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final AuthService authService;
    private final UserService userService;

    public PostController(PostService postService, CommentService commentService, AuthService authService, UserService userService){
        this.postService = postService;
        this.commentService = commentService;
        this.authService = authService;
        this.userService = userService;
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getOnePost(@PathVariable("postId") Long postId, @RequestParam(defaultValue = "true") boolean comments, @RequestParam(defaultValue = "true") boolean personalized){
        String userId = personalized?authService.getCurrentUserId():null;

        var post = postService.getPostDto(postId, userId, comments);
        return ResponseEntity.ok().body(post);
    }

    /**
     * versatile endpoint for getting paginated and sorted posts.
     */
    @GetMapping
    public ResponseEntity<Page<PostDto>> getFilteredPosts(@RequestParam(required = false) String authorId,
                                                       @RequestParam(required = false) List<Long> tagIds,
                                                       @RequestParam(required = false) LocalDateTime minDate,
                                                       @RequestParam(required = false) LocalDateTime maxDate,
                                                       @RequestParam(required = false) String title,
                                                       @RequestParam(defaultValue = "postedDate") String sortBy,
                                                       @RequestParam(defaultValue = "DESC") String sortDirection,
                                                       @RequestParam(defaultValue = "0") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer size) {

        String userId = authService.getCurrentUserId();
        Page<PostDto> posts = postService.getFilteredPosts(authorId, tagIds, minDate, maxDate, title,
                sortBy, sortDirection, page, size, userId);
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    /**
     * method for creating new post
     * it gets author from authorization for safety reasons
     * @param postRequestDto profile data
     */
    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody PostRequestDto postRequestDto){
        String userId = authService.getCurrentUserId();

        User author = userService.getUserById(userId)
                .orElseThrow(()->new ForbiddenException("User not found"));

        PostDto postDto = postService.createPost(author, postRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(postDto);
    }

    /**
     * method for updating post
     * it gets author from authorization for safety reasons
     * @param postRequestDto new profile data
     */
    @PatchMapping("/{postId}")
    public ResponseEntity<PostDto> updatePost(@RequestBody PostRequestDto postRequestDto, @PathVariable("postId") Long postId){
        String userId = authService.getCurrentUserId();

        PostDto postDto = postService.updatePost(userId, postRequestDto, postId);
        return ResponseEntity.status(HttpStatus.OK).body(postDto);
    }

    /**
     * method for deleting post
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable("postId") Long postId){
        String userId = authService.getCurrentUserId();

        postService.deletePost(userId, postId);
        return ResponseEntity.ok().build();
    }

    /**
     * method for toggling like
     * @param postId id of post to like
     * @return isLiked with value true if post is liked and false otherwise
     */
    @PatchMapping("/{postId}/toggle-like")
    public ResponseEntity<?> toggleLike(@PathVariable("postId") Long postId){
        String userId = authService.getCurrentUserId();

        User user = userService.getUserById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        boolean isLiked = postService.toggleLike(user, postId);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("isLiked", isLiked);

        return ResponseEntity.ok(responseBody);
    }

    /**
     * method for getting all comments of post
     * @param postId id of post
     * @return comments tree
     */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<Set<CommentDto>> getCommentsOfPost(@PathVariable("postId") Long postId){
        String userId = authService.getCurrentUserId();

        return ResponseEntity.ok(postService.getPostCommentTree(postId, userId));
    }


    /**
     * method for creating comment
     * @param postId id of post which user is commenting
     * @param replyTo id of comment to reply to (can be null)
     * @return created comment
     */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentDto> createComment(@PathVariable("postId") Long postId,
                                                    @RequestParam(name = "replyTo", required = false) Long replyTo,
                                                    @RequestBody CommentRequestDto commentRequestDto){
        String userId = authService.getCurrentUserId();

        User user = userService
                .getUserById(userId)
                .orElseThrow(()->new EntityNotFoundException("No such user"));

        Post post = postService
                .getPostById(postId)
                .orElseThrow(()->new EntityNotFoundException("No such post"));

        Comment comment = new Comment();
        comment.setCommentAuthor(user);
        comment.setPost(post);
        comment.setContent(commentRequestDto.content());
        if(replyTo!=null) {
            Comment replyToComment = commentService
                    .getCommentById(replyTo)
                    .orElseThrow(()->new EntityNotFoundException("Can't find comment to attach this to"));
            comment.setReplyTo(replyToComment);
        }

        return ResponseEntity.ok(commentService.save(comment));
    }


}
