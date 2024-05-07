package com.naukma.thesisbackend.services;

import com.naukma.thesisbackend.dtos.PostDto;
import com.naukma.thesisbackend.entities.Post;
import com.naukma.thesisbackend.repositories.PostRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommentService commentService;

    public PostService(
            PostRepository postRepository,
            CommentService commentService
    ){
        this.postRepository = postRepository;
        this.commentService = commentService;
    }

    /**
     * method for mapping {@link Post Post} into {@link PostDto PostDto} object
     * @param post post object
     * @param userId id of user, for personalized response. If it is not null, the object will contain likes left by this specific user
     * @param comments true if returning object has to have comments, false otherwise
     * @return {@link PostDto PostDto} object
     */
    public PostDto postToPostDto(Post post, String userId, boolean comments){

        boolean isLiked = userId != null && !userId.isEmpty() && (post
                .getPostLikes()
                .stream()
                .anyMatch(like -> Objects.equals(like.getUser().getUserId(), userId)));

        return new PostDto(
                post.getPostId(),
                post.getTitle(),
                post.getContent(),
                post.getPostedDate(),
                post.getPostLikes().size(),
                comments?post.getComments().stream().filter(comment -> comment.getReplies()!=null).map(comment -> commentService.commentToCommentDto(comment, userId)).collect(Collectors.toSet()):null,
                isLiked,
                post.getPostAuthor().getUserId(),
                post.getPostAuthor().getNickname());
    }

    /**
     * method for getting one post from repository
     * @param postId id of queried post
     * @return {@link Post Post} object
     */
    public PostDto getPostDto(Long postId, String userId, boolean comments){
        Post post = postRepository.findPostByPostId(postId)
                .orElseThrow(() -> new EntityNotFoundException("no post with this id"));

        return postToPostDto(post, userId, comments);
    }

    /**
     * method for querying several posts
     * @param authorId id of author post, or its part
     * @param tagId ids
     * @param minDate
     * @param maxDate
     * @param title
     * @param sortByDate
     * @param page
     * @param size
     * @param userId
     * @return
     */
    public Page<PostDto> getFilteredPosts(Long authorId,
                                          List<Long> tagIds,
                                          LocalDateTime minDate,
                                          LocalDateTime maxDate,
                                          String title,
                                          boolean sortByDate,
                                          int page,
                                          int size,
                                          String userId
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findFilteredPosts(authorId, tagIds, minDate, maxDate, title,
                sortByDate, pageable).map(post -> postToPostDto(post, userId, false));
    }
}
