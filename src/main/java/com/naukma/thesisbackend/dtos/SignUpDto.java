package com.naukma.thesisbackend.dtos;

public record SignUpDto(
        String userId,
        String nickname,
        String email,
        String password
) {
}
