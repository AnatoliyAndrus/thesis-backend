package com.naukma.thesisbackend.services;

import com.naukma.thesisbackend.dtos.CommentDto;
import com.naukma.thesisbackend.entities.*;
import com.naukma.thesisbackend.exceptions.ForbiddenException;
import com.naukma.thesisbackend.repositories.CommentLikeRepository;
import com.naukma.thesisbackend.repositories.CommentRepository;
import com.naukma.thesisbackend.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository, CommentLikeRepository commentLikeRepository){
        this.commentRepository = commentRepository;

        this.userRepository = userRepository;
        this.commentLikeRepository = commentLikeRepository;
    }

    /**
     * recursively maps comment object to commentDto object
     * @param comment comment entity object
     * @param userId id of currently registered user (for getting likes of user)
     * @return
     */
    public CommentDto commentToCommentDto(Comment comment, String userId){

        Set<CommentDto> replies = new HashSet<>();
        if(comment.getReplies()!=null&&!comment.getReplies().isEmpty()){
            replies = comment
                    .getReplies()
                    .stream()
                    .map(reply->commentToCommentDto(reply, userId))
                    .collect(Collectors.toSet());
        }

        boolean isLiked = userId != null && !userId.isEmpty() && (comment
                .getCommentLikes()
                .stream()
                .anyMatch(like -> Objects.equals(like.getUser().getUserId(), userId)));

        return new CommentDto(
                comment.getCommentId(),
                comment.getContent(),
                comment.getCommentAuthor().getUserId(),
                comment.getCommentAuthor().getNickname(),
                comment.isEdited(),
                replies,
                comment.getCommentLikes().size(),
                isLiked
                );

    }


    /**
     * method for retrieving comment by its ID
     * @param commentId id of comment
     * @return optional of {@link Comment}
     */
    public Optional<Comment> getCommentById(Long commentId){
        return commentRepository.findCommentByCommentId(commentId);
    }

    /**
     * saves comment to database
     * @param comment comment to save
     * @return saved comment as {@link CommentDto}
     */
    public CommentDto save(Comment comment){
        return commentToCommentDto(commentRepository.save(comment), null);
    }

    public void delete(Comment comment){
        commentRepository.delete(comment);
    }

    /**
     * verifies if comment with this id belongs to user
     * @param commentId id of comment
     * @param userId id of user
     * @return verified comment
     */
    public Comment verifyCommentOwnership(Long commentId, String userId) {
        Comment comment = getCommentById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("No such comment found"));

        if (!Objects.equals(comment.getCommentAuthor().getUserId(), userId)) {
            throw new ForbiddenException("User must be the author of the comment to delete it");
        }
        return comment;
    }

    public boolean toggleLike(String userId, Long commentId){
        User user = userRepository
                .findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Comment comment = commentRepository
                .findCommentByCommentId(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        Optional<CommentLike> commentLike = commentLikeRepository
                .findByUserAndComment(user, comment);

        if(commentLike.isPresent()){
            commentLikeRepository.delete(commentLike.get());
            return false;
        }
        else{
            CommentLike newCommentLike = new CommentLike(user, comment);
            commentLikeRepository.save(newCommentLike);
            return true;
        }
    }
}
