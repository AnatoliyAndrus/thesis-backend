package com.naukma.thesisbackend.entities;

import com.naukma.thesisbackend.entities.keys.CommentLikeKey;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "comment_like")
public class CommentLike {
    @EmbeddedId
    private CommentLikeKey id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("commentId")
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @CreationTimestamp
    @JoinColumn(name = "like_date")
    private LocalDateTime likeDate;

}