package com.naukma.thesisbackend.controllers;

import com.naukma.thesisbackend.dtos.CommentDto;
import com.naukma.thesisbackend.dtos.CommentRequestDto;
import com.naukma.thesisbackend.entities.Comment;
import com.naukma.thesisbackend.services.AuthService;
import com.naukma.thesisbackend.services.CommentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/comments")
public class CommentController {

    private final AuthService authService;
    private final CommentService commentService;

    public CommentController(AuthService authService, CommentService commentService) {
        this.authService = authService;
        this.commentService = commentService;
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> getOneComment(@PathVariable("commentId") Long commentId){
        String userId = authService.getCurrentUserId();

        Comment comment = commentService
                .getCommentById(commentId)
                .orElseThrow(()->new EntityNotFoundException("No such comment found"));

        return ResponseEntity
                .ok(commentService.commentToCommentDto(comment, userId));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable("commentId") Long commentId, @RequestBody CommentRequestDto commentRequestDto){
        String userId = authService.getCurrentUserId();

        Comment comment = commentService
                .verifyCommentOwnership(commentId, userId);

        CommentDto updatedComment = commentService.save(comment);
        return ResponseEntity
                .ok(updatedComment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable("commentId") Long commentId){
        String userId = authService.getCurrentUserId();

        Comment comment = commentService.verifyCommentOwnership(commentId, userId);

        commentService.delete(comment);
        return ResponseEntity.ok().build();
    }
}
