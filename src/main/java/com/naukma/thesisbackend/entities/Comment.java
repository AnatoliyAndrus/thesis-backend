package com.naukma.thesisbackend.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
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

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "commented_date")
    @CreationTimestamp
    private LocalDateTime commentedDate;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
//    @JsonIgnore
    private Post post;

    @OneToMany(mappedBy = "comment", orphanRemoval = true)
    private Set<CommentLike> commentLikes;

    @ManyToOne
    @JoinColumn(referencedColumnName = "comment_id", nullable = true)
//    @JsonBackReference
    private Comment replyTo;

    @OneToMany(mappedBy = "replyTo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> replies;

    /**
     * if the comment was edited, this field becomes true, false otherwise
     */
    @Column(name = "edited")
    private boolean edited = false;


    /**
     * listener which toggles {@link #edited edited} field to true if comment is updated
     */
    @PrePersist
    @PreUpdate
    public void beforeSave() {
        this.edited = true;
    }
}