package com.naukma.thesisbackend.dtos;

import java.time.LocalDateTime;
import java.util.Set;

public record PostDto(
    Long postId,
    String title,
    String content,
    LocalDateTime postedDate,
    int likes,
    Set<CommentDto> commentsTree,
    boolean isLiked,
    String authorUserId,
    String authorNickname
) {
}
