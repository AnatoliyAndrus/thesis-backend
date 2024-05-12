package com.naukma.thesisbackend.controllers;

import com.naukma.thesisbackend.dtos.CommentDto;
import com.naukma.thesisbackend.dtos.CommentRequestDto;
import com.naukma.thesisbackend.entities.Comment;
import com.naukma.thesisbackend.entities.User;
import com.naukma.thesisbackend.exceptions.ForbiddenException;
import com.naukma.thesisbackend.services.AuthService;
import com.naukma.thesisbackend.services.CommentService;
import com.naukma.thesisbackend.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("api/v1/comments")
public class CommentController {

    private final AuthService authService;
    private final CommentService commentService;
    private final UserService userService;

    public CommentController(AuthService authService, CommentService commentService, UserService userService) {
        this.authService = authService;
        this.commentService = commentService;
        this.userService = userService;
    }

    /**
     * retrieves one comment as {@link CommentDto} object
     * @param commentId id of comment
     * @return comment
     */
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> getOneComment(@PathVariable("commentId") Long commentId){
        String userId = authService.getCurrentUserId();

        Comment comment = commentService
                .getCommentById(commentId)
                .orElseThrow(()->new EntityNotFoundException("No such comment found"));

        return ResponseEntity
                .ok(commentService.commentToCommentDto(comment, userId));
    }

    /**
     * updates comment in database
     * requires authenticated user to be the same as the author of comment
     * @param commentId id of comment
     * @param commentRequestDto new comment content
     * @return updated comment
     */
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable("commentId") Long commentId, @RequestBody CommentRequestDto commentRequestDto){
        String userId = authService.getCurrentUserId();

        Comment comment = commentService.verifyCommentOwnership(commentId, userId);
        comment.setEdited(true);

        CommentDto updatedComment = commentService.save(comment);
        return ResponseEntity
                .ok(updatedComment);
    }

    /**
     * deletes comment from database
     * requires authenticated user to be the same as the author of comment
     * @param commentId id of comment
     * @return HTTP response 200 without body
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable("commentId") Long commentId){
        String userId = authService.getCurrentUserId();

        Comment comment = commentService.verifyCommentOwnership(commentId, userId);

        commentService.delete(comment);
        return ResponseEntity.ok().build();
    }

    /**
     * sets/removes like from comment
     * requires authenticated user to perform
     * @param commentId id of comment
     * @return true if comment is now liked, false otherwise
     */
    @PatchMapping("/{commentId}/toggle-like")
    public ResponseEntity<?> toggleLike(@PathVariable("commentId") Long commentId){
        String userId = authService.getCurrentUserId();

        User user = userService.getUserById(userId)
                .orElseThrow(()->new EntityNotFoundException("User not found"));

        boolean isLiked = commentService.toggleLike(user, commentId);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("isLiked", isLiked);

        return ResponseEntity.ok(responseBody);
    }
}
