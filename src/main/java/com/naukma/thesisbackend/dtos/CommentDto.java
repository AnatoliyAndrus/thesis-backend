package com.naukma.thesisbackend.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record CommentDto(
    Long commentId,
    Long postId,
    String content,
    String authorUserId,
    String authorNickname,
    boolean edited,
    List<CommentDto> replies,
    Long replyTo,
    int likes,
    boolean isLiked,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime commentedDate
) {
}
