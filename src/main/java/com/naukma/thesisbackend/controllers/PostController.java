package com.naukma.thesisbackend.controllers;

import com.naukma.thesisbackend.dtos.PostDto;
import com.naukma.thesisbackend.entities.Post;
import com.naukma.thesisbackend.services.AuthService;
import com.naukma.thesisbackend.services.PostService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/v1/posts")
public class PostController {

    private final PostService postService;
    private final AuthService authService;

    public PostController(PostService postService, AuthService authService){
        this.postService = postService;
        this.authService = authService;
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getOnePost(@PathVariable("postId") Long postId){
        String userId = authService.getCurrentUserId();

        var post = postService.getPostDto(postId, userId, true);
        return ResponseEntity.ok().body(post);
    }

    @GetMapping
    public ResponseEntity<Page<PostDto>> getFilteredPosts(@RequestParam(required = false) Long authorId,
                                                       @RequestParam(required = false) List<Long> tagIds,
                                                       @RequestParam(required = false) LocalDateTime minDate,
                                                       @RequestParam(required = false) LocalDateTime maxDate,
                                                       @RequestParam(required = false) String title,
                                                       @RequestParam(required = false, defaultValue = "false") Boolean sortByLikes,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {

        String userId = authService.getCurrentUserId();
        Page<PostDto> posts = postService.getFilteredPosts(authorId, tagIds, minDate, maxDate, title,
                sortByLikes, page, size, userId);
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }
}
