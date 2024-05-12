package com.naukma.thesisbackend.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Table(name = "comment")
@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "commented_date")
    @CreationTimestamp
    private LocalDateTime commentedDate;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
//    @JsonIgnore
    private Post post;

    @OneToMany(mappedBy = "comment", orphanRemoval = true)
    private Set<CommentLike> commentLikes = new HashSet<>();

    @ManyToOne
    @JoinColumn(referencedColumnName = "comment_id", nullable = true)
//    @JsonBackReference
    private Comment replyTo;

    @OneToMany(mappedBy = "replyTo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> replies = new HashSet<>();


    /**
     * user, who created this comment
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
//    @JsonIgnore
    private User commentAuthor;

    /**
     * if the comment was edited, this field becomes true, false otherwise
     */
    @Column(name = "edited")
    private boolean edited = false;

}