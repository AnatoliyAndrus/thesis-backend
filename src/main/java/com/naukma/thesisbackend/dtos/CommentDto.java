package com.naukma.thesisbackend.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Set;

public record CommentDto(
    Long commentId,
    String content,
    String authorUserId,
    String authorNickname,
    boolean edited,
    Set<CommentDto> replies,
    int likes,
    boolean isLiked,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime commentedDate
) {
}
