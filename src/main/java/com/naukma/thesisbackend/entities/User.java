package com.naukma.thesisbackend.entities;

import com.naukma.thesisbackend.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

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
     * avatar of user, encoded in byte array.
     * can be null
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
     * posts liked by user
     */
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private Set<PostLike> postLikes;

    /**
     * comments liked by user
     */
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private Set<CommentLike> commentLikes;

    /**
     * posts which user is author of
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Post> posts;

    @Column(name = "registered_date")
    @CreationTimestamp
    private LocalDateTime registeredDate;

}
