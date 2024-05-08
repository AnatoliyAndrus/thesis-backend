package com.naukma.thesisbackend.entities;

import com.naukma.thesisbackend.entities.keys.PostLikeKey;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * like left by user under the post
 */
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "post_like")
public class PostLike {
    @EmbeddedId
    private PostLikeKey id;

    public PostLike(User user, Post post){
        this.user = user;
        this.post = post;
    }

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
