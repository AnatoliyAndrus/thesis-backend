package com.naukma.thesisbackend.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.naukma.thesisbackend.dtos.UserBasicInfoDto;
import com.naukma.thesisbackend.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "app-user")
@NoArgsConstructor
public class User {

    public User(String userId, String nickname, String email, String password, UserRole role) {
        this.userId = userId;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    /**
     * represents unique string ID of user which is chosen by user
     */
    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * represents displayed nickname of user
     */
    @Column(name = "nickname", nullable = false)
    private String nickname;

    /**
     * represents email of user
     */
    @Column(name = "email", nullable = false)
    private String email;

    /**
     * role of user
     */
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    /**
     * avatar image of user
     * can be null
     */
    @Column(name = "avatar", nullable = true)
    @JsonIgnore
    private String avatar;

    /**
     * password of user
     */
    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String password;

    /**
     * posts liked by user
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<PostLike> postLikes;

    /**
     * comments liked by user
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<CommentLike> commentLikes = new HashSet<>();

    /**
     * posts which user is author of
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "postAuthor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Post> posts = new HashSet<>();

    /**
     * comments which user is author of
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "commentAuthor", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Comment> comments = new HashSet<>();


    @Column(name = "registered_date")
    @CreationTimestamp
    private LocalDateTime registeredDate;

    /**
     * method for mapping {@link User User} object into {@link UserBasicInfoDto UserBasicInfoDto} record
     * @return {@link UserBasicInfoDto UserBasicInfoDto} record
     */
    public UserBasicInfoDto toUserBasicInfoDto(){
        return new UserBasicInfoDto(
                this.getUserId(),
                this.getNickname(),
                this.getPosts().stream().map(Post::getPostId).collect(Collectors.toSet()),
                this.getRegisteredDate()
        );
    }
}
