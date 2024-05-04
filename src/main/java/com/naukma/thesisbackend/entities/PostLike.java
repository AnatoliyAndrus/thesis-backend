package com.naukma.thesisbackend.entities;

import com.naukma.thesisbackend.entities.keys.PostLikeKey;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * like left by user under the post
 */
@Getter
@Setter
@Entity
@Table(name = "post_like")
public class PostLike {
    @EmbeddedId
    private PostLikeKey id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    private Post post;

    @CreationTimestamp
    @JoinColumn(name = "like_date")
    private LocalDateTime likeDate;

}
