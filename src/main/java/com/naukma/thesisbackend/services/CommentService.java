package com.naukma.thesisbackend.services;

import com.naukma.thesisbackend.dtos.CommentDto;
import com.naukma.thesisbackend.entities.*;
import com.naukma.thesisbackend.entities.keys.CommentLikeKey;
import com.naukma.thesisbackend.exceptions.ForbiddenException;
import com.naukma.thesisbackend.repositories.CommentLikeRepository;
import com.naukma.thesisbackend.repositories.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    public CommentService(CommentRepository commentRepository,
                          CommentLikeRepository commentLikeRepository){
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
    }

    /**
     * recursively maps comment object to commentDto object
     * @param comment comment entity object
     * @param userId id of currently registered user (for personalized response)
     * @return Comment Dto
     */
    public CommentDto commentToCommentDto(Comment comment,
                                          @Nullable String userId){

        List<CommentDto> replies = new ArrayList<>();
        if(comment.getReplies()!=null&&!comment.getReplies().isEmpty()){
            replies = comment
                    .getReplies()
                    .stream()
                    .sorted(Comparator.comparing(Comment::getCommentedDate).reversed())
                    .map(reply->commentToCommentDto(reply, userId))
                    .toList();
        }

        boolean isLiked = userId != null && !userId.isEmpty() && (comment
                .getCommentLikes()
                .stream()
                .anyMatch(like -> Objects.equals(like.getUser().getUserId(), userId)));

        return new CommentDto(
                comment.getCommentId(),
                comment.getPost().getPostId(),
                comment.getContent(),
                comment.getCommentAuthor().getUserId(),
                comment.getCommentAuthor().getNickname(),
                comment.isEdited(),
                replies,
                comment.getReplyTo()!=null?comment.getReplyTo().getCommentId():null,
                comment.getCommentLikes().size(),
                isLiked,
                comment.getCommentedDate()
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

    /**
     * deletes comment from database
     * @param comment comment to delete
     */
    public void delete(Comment comment){
        commentRepository.delete(comment);
    }

    /**
     * sets/removes like from comment
     * @param user user who performs this operation
     * @param commentId id of comment
     * @return true if comment is now liked, false otherwise
     */
    public boolean toggleLike(User user, Long commentId){
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
            newCommentLike.setId(new CommentLikeKey());
            commentLikeRepository.save(newCommentLike);
            return true;
        }
    }

    /**
     * verifies if comment with this id belongs to user adn returns this comment
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
}
