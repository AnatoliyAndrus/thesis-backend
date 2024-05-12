package com.naukma.thesisbackend.services;

import com.naukma.thesisbackend.dtos.CommentDto;
import com.naukma.thesisbackend.dtos.PostDto;
import com.naukma.thesisbackend.dtos.PostRequestDto;
import com.naukma.thesisbackend.entities.Comment;
import com.naukma.thesisbackend.entities.Post;
import com.naukma.thesisbackend.entities.PostLike;
import com.naukma.thesisbackend.entities.User;
import com.naukma.thesisbackend.entities.keys.PostLikeKey;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final TagRepository tagRepository;
    private final CommentService commentService;

    public PostService(
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            TagRepository tagRepository,
            CommentService commentService
    ){
        this.postRepository = postRepository;
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

        boolean isLiked = userId != null
                && !userId.isEmpty() && (post
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
                post.getPostAuthor().getNickname(),
                post.getTags());
    }


    private List<CommentDto> getPostCommentTree(Post post, @Nullable String userId){
        return post
                .getComments()
                .stream()
                .filter(comment -> comment.getReplyTo()==null)
                .sorted(Comparator.comparing(Comment::getCommentedDate).reversed())
                .map(comment -> commentService.commentToCommentDto(comment, userId))
                .toList();
    }

    /**
     * method for converting post comments into {@link Set<CommentDto>} list for returning as response
     * @param postId post id
     * @param userId id of current user
     * @return {@link List<CommentDto>} list
     */
    public List<CommentDto> getPostCommentTree(Long postId, @Nullable String userId){
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
     * versatile method for querying posts by several parameters
     *
     * @param authorId id of post author
     * @param tagIds tags of post
     * @param minDate minimal date of post creation
     * @param maxDate minimal date of post creation
     * @param title title of post
     * @param sortBy column by which posts will be sorted, for example "
     * @param sortDirection direction of sorting, can be "ASC" or "DESC"
     * @param page number of queried page
     * @param size size of page
     * @param userId id of current user (for personalizing queried posts)
     * @return page of posts
     */
    public Page<PostDto> getFilteredPosts(String authorId,
                                          @Nullable List<Long> tagIds,
                                          @Nullable LocalDateTime minDate,
                                          @Nullable LocalDateTime maxDate,
                                          @Nullable String title,
                                          @Nullable String sortBy,
                                          @Nullable String sortDirection,
                                          Integer page,
                                          Integer size,
                                          @Nullable String userId
    ) {
        //Setting up sorting and pagination
        Pageable pageable;

        if(!Objects.equals(sortBy, "likes")) {
            Sort.Order sortOrder = new Sort.Order(
                    (sortDirection == null || sortDirection.equalsIgnoreCase("DESC")) ? Sort.Direction.DESC : Sort.Direction.ASC,
                    (sortBy == null) ? "postedDate" : sortBy);
            pageable = PageRequest.of(page, size, Sort.by(sortOrder));

            return postRepository
                    .findFilteredPosts(authorId, tagIds, minDate, maxDate, title, pageable)
                    .map(post -> postToPostDto(post, userId, false));
        }
        else {
            pageable = PageRequest.of(page, size);
            return postRepository
                    .findFilteredPostsSortByLikes(authorId, tagIds, minDate, maxDate, title, pageable)
                    .map(objectList -> postToPostDto((Post)objectList[0], userId, false));
        }
    }


    /**
     * creating post of user
     * @param author author of post
     * @param postRequestDto post request
     * @return created post as {@link PostDto} object
     */
    public PostDto createPost(User author, PostRequestDto postRequestDto) {
        Post post = new Post();
        post.setPostAuthor(author);
        post.setTitle(postRequestDto.title());
        post.setContent(postRequestDto.content());
        post.setTags(new ArrayList<>(tagRepository.findAllById(postRequestDto.tags())));

        return postToPostDto(postRepository.save(post), author.getUserId(), false);
    }

    /**
     * updates post
     * @param userId id of user
     * @param postRequestDto updated post
     * @param postId id of post
     * @return updated post as {@link PostDto} object
     */
    public PostDto updatePost(String userId, PostRequestDto postRequestDto, Long postId) {
        Post post = postRepository.findPostByPostId(postId)
                .orElseThrow(()->new EntityNotFoundException("Post with this ID not found"));

        if(!Objects.equals(post.getPostAuthor().getUserId(), userId)){
            throw new ForbiddenException("Requested post doesn't belong to authenticated user");
        }

        post.setTitle(postRequestDto.title());
        post.setContent(postRequestDto.content());
        post.setTags(new ArrayList<>(tagRepository.findAllById(postRequestDto.tags())));

        return postToPostDto(postRepository.save(post), userId, false);
    }

    /**
     * deletes post. throws exception if post doesn't belong to person
     * @param userId id of user
     * @param postId id of post
     */
    public void deletePost(String userId, Long postId) {
        Post post = postRepository.findPostByPostId(postId)
                .orElseThrow(()->new EntityNotFoundException("Post with this ID not found"));

        if(!Objects.equals(post.getPostAuthor().getUserId(), userId)){
            throw new ForbiddenException("Requested post doesn't belong to authenticated user");
        }

        postRepository
                .delete(post);
    }

    /**
     * method for setting/removing like under the post
     * @param user current user id, needed for checking if post belongs to current user
     * @param postId id of post
     * @return true if post is now liked, false otherwise
     */
    public boolean toggleLike(User user, Long postId){
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
            newPostLike.setId(new PostLikeKey());
            postLikeRepository.save(newPostLike);
            return true;
        }
    }

    /**
     * gets post from database
     * @param postId id of post
     * @return requested post
     */
    public Optional<Post> getPostById(Long postId){
        return postRepository.findPostByPostId(postId);
    }
}
