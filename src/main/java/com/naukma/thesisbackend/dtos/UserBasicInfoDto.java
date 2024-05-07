package com.naukma.thesisbackend.dtos;


import java.time.LocalDateTime;
import java.util.Set;

/**
 * Dto for getting non-sensitive user info from server
 * @param userId id of user
 * @param nickname nickname of user
 * @param postIds identifiers of posts written by this user
 * @param registeredDate exact time when user was registered (generated automatically)
 */
public record UserBasicInfoDto (
        String userId,
        String nickname,
        Set<Long> postIds,
        LocalDateTime registeredDate
){

}
