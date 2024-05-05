package com.naukma.thesisbackend.enums;

/**
 * enum, which represents role of user.
 * If user has role "USER", he can access public endpoints
 * If user has role "ADMIN", he has access any endpoint
 */
public enum UserRole {
    USER,
    ADMIN
}