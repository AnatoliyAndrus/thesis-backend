package com.naukma.thesisbackend.entities;

import com.naukma.thesisbackend.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User {

    /**
     * represents unique string ID of user which is chosen by user
     */
    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * represents first name of user
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * represents last name of user
     */
    @Column(name = "surname", nullable = false)
    private String surname;

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
     * avatar of user, encoded in byte array.
     */
    @Column(name = "avatar", columnDefinition = "BLOB")
    @Lob
    private byte[] avatar;

    /**
     * password of user
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * likes left by user under the posts
     */
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private Set<PostLike> postLikes;

    /**
     * likes left by user under the comments
     */
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private Set<CommentLike> commentLikes;

    /**
     * posts which user liked
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Post> posts;

}
