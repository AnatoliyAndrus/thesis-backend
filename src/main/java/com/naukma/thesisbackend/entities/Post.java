package com.naukma.thesisbackend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * post of user. Each post has title and content
 */
@Getter
@Setter
@Entity
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    /**
     * title of post
     */
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * text content of post
     */
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "posted_date")
    @CreationTimestamp
    private LocalDateTime postedDate;

    /**
     * likes of post
     */
    @OneToMany(mappedBy = "post", orphanRemoval = true)
    private Set<PostLike> postLikes;

    /**
     * user, who created this post
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
//    @JsonIgnore
    private User author;

    /**
     * comments of post
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments;

    /**
     * tags of post
     */
    @ManyToMany
    @JoinTable(
            name = "post_tag",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags;
}
