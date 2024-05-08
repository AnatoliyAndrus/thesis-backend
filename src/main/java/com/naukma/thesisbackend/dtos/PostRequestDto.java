package com.naukma.thesisbackend.dtos;

import java.util.Set;

/**
 * Simple post DTO made for POST and UPDATE methods on posts
 * @param title title of post
 * @param content content of post
 * @param tags tags of post
 */
public record PostRequestDto(
        String title,
        String content,
        Set<Long> tags
) {
}
