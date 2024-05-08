package com.naukma.thesisbackend.controllers;

import com.naukma.thesisbackend.dtos.PostDto;
import com.naukma.thesisbackend.dtos.PostRequestDto;
import com.naukma.thesisbackend.services.AuthService;
import com.naukma.thesisbackend.services.PostService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<PostDto> getOnePost(@PathVariable("postId") Long postId, @RequestParam(defaultValue = "true") boolean comments, @RequestParam(defaultValue = "true") boolean personalized){
        String userId = personalized?authService.getCurrentUserId():null;

        var post = postService.getPostDto(postId, userId, comments);
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

    /**
     * method for creating new post
     * it gets author from authorization for safety reasons
     * @param postRequestDto profile data
     */
    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody PostRequestDto postRequestDto){
        String userId = authService.getCurrentUserId();

        PostDto postDto = postService.createPost(userId, postRequestDto);
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

    @PatchMapping("/{postId}/toggle-like")
    public ResponseEntity<?> toggleLike(@PathVariable("postId") Long postId){
        String userId = authService.getCurrentUserId();

        boolean isLiked = postService.toggleLike(userId, postId);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("isLiked", isLiked);

        return ResponseEntity.ok(responseBody);
    }
}
