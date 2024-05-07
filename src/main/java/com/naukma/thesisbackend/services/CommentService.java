package com.naukma.thesisbackend.services;

import com.naukma.thesisbackend.dtos.CommentDto;
import com.naukma.thesisbackend.entities.Comment;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private UserService userService;

    public CommentService(UserService userService){
        this.userService = userService;
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
}
