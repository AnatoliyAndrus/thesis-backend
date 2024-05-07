package com.naukma.thesisbackend.dtos;

import java.util.Set;

public record CommentDto(
    Long commendId,
    String content,
    String authorUserId,
    String authorNickname,
    boolean edited,
    Set<CommentDto> replies,
    int likes,
    boolean isLiked
) {
}
