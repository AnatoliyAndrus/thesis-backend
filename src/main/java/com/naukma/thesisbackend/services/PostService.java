package com.naukma.thesisbackend.services;

import com.naukma.thesisbackend.dtos.CommentDto;
import com.naukma.thesisbackend.dtos.PostDto;
import com.naukma.thesisbackend.dtos.PostRequestDto;
import com.naukma.thesisbackend.entities.Post;
import com.naukma.thesisbackend.entities.PostLike;
import com.naukma.thesisbackend.entities.Tag;
import com.naukma.thesisbackend.entities.User;
import com.naukma.thesisbackend.exceptions.ForbiddenException;
import com.naukma.thesisbackend.repositories.PostLikeRepository;
import com.naukma.thesisbackend.repositories.PostRepository;
import com.naukma.thesisbackend.repositories.TagRepository;
import com.naukma.thesisbackend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final TagRepository tagRepository;
    private final CommentService commentService;

    public PostService(
            PostRepository postRepository,
            UserRepository userRepository, PostLikeRepository postLikeRepository, TagRepository tagRepository, CommentService commentService
    ){
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postLikeRepository = postLikeRepository;
        this.tagRepository = tagRepository;
        this.commentService = commentService;
    }

    /**
     * method for mapping {@link Post Post} into {@link PostDto PostDto} object
     * @param post post object
     * @param userId id of user, for personalized response. If it is not null, the object will contain likes left by this specific user
     * @param comments true if returning object has to have comments, false otherwise
     * @return {@link PostDto PostDto} object
     */
    public PostDto postToPostDto(Post post, @Nullable String userId, boolean comments){

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
                comments?getPostCommentTree(post, userId):null,
                isLiked,
                post.getPostAuthor().getUserId(),
                post.getPostAuthor().getNickname());
    }

    /**
     * method for converting post database comment list into {@link Set<CommentDto>} list for returning as response
     * @param post post
     * @param userId id of current user
     * @return {@link Set<CommentDto>} list
     */
    public Set<CommentDto> getPostCommentTree(Post post, @Nullable String userId){
        return post
                .getComments()
                .stream()
                .filter(comment -> comment.getReplies()!=null)
                .map(comment -> commentService.commentToCommentDto(comment, userId))
                .collect(Collectors.toSet());
    }

    public Set<CommentDto> getPostCommentTree(Long postId, @Nullable String userId){
        Post post = postRepository.findPostByPostId(postId).orElseThrow(()-> new EntityNotFoundException("No such post"));

        return getPostCommentTree(post, userId);
    }

    /**
     * method for getting one post from repository
     * @param postId id of queried post
     * @return {@link PostDto PostDto} object
     */
    public PostDto getPostDto(Long postId, String userId, boolean comments){
        Post post = postRepository.findPostByPostId(postId)
                .orElseThrow(() -> new EntityNotFoundException("no post with this id"));

        return postToPostDto(post, userId, comments);
    }

    /**
     * method for querying several posts
     * @param authorId id of author post, or its part
     * @param tagIds ids
     * @param minDate minimal date when posted
     * @param maxDate maximal date when posted
     * @param title title of post
     * @param sortByDate if posts should be sorted by date (otherwise they are sorted by amount of likes)
     * @param page number of queried page
     * @param size amount of posts on queried page
     * @param userId id of current user (can be null)
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
                                          @Nullable String userId
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findFilteredPosts(authorId, tagIds, minDate, maxDate, title,
                sortByDate, pageable).map(post -> postToPostDto(post, userId, false));
    }

    public PostDto createPost(String userId, PostRequestDto postRequestDto) {
        User author = userRepository.findByUserId(userId)
                .orElseThrow(()->new ForbiddenException("No such user found"));

        Post post = new Post();
        post.setPostAuthor(author);
        post.setTitle(postRequestDto.title());
        post.setContent(postRequestDto.content());
        post.setTags(new HashSet<>(tagRepository.findAllById(postRequestDto.tags())));

        return postToPostDto(postRepository.save(post), userId, false);
    }

    public PostDto updatePost(String userId, PostRequestDto postRequestDto, Long postId) {
        User author = userRepository.findByUserId(userId)
                .orElseThrow(()->new ForbiddenException("No such user found"));

        Post post = postRepository.findPostByPostId(postId)
                .orElseThrow(()->new EntityNotFoundException("Post with this ID not found"));

        if(!Objects.equals(post.getPostAuthor().getUserId(), userId)){
            throw new ForbiddenException("Requested post doesn't belong to authenticated user");
        }

        post.setTitle(postRequestDto.title());
        post.setContent(postRequestDto.content());
        post.setTags(new HashSet<>(tagRepository.findAllById(postRequestDto.tags())));

        return postToPostDto(postRepository.save(post), userId, false);
    }


    public void deletePost(String userId, Long postId) {

        Post post = postRepository.findPostByPostId(postId)
                .orElseThrow(()->new EntityNotFoundException("Post with this ID not found"));

        if(!Objects.equals(post.getPostAuthor().getUserId(), userId)){
            throw new ForbiddenException("Requested post doesn't belong to authenticated user");
        }

        postRepository
                .delete(post);
    }

    public boolean toggleLike(String userId, Long postId){
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("User not found"));

        Post post = postRepository
                .findPostByPostId(postId)
                .orElseThrow(() -> new EntityNotFoundException("No such post"));

        Optional<PostLike> postLike = postLikeRepository.
                findByUserAndPost(user, post);

        if(postLike.isPresent()){
            postLikeRepository.delete(postLike.get());
            return false;
        }
        else{
            PostLike newPostLike = new PostLike(user, post);
            postLikeRepository.save(newPostLike);
            return true;
        }
    }
}
