package com.naukma.thesisbackend.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.naukma.thesisbackend.entities.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record PostDto(
    Long postId,
    String title,
    String content,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime postedDate,
    int likes,
    Set<CommentDto> comments,
    boolean isLiked,
    String authorUserId,
    String authorNickname,
    Set<Tag> tags
) {
}
