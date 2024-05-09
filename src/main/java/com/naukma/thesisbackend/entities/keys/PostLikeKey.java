package com.naukma.thesisbackend.entities.keys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
public class PostLikeKey implements Serializable {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "post_id")
    private Long postId;


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final PostLikeKey other = (PostLikeKey) obj;

        return this.userId.equals(other.userId)&&this.postId.equals(other.postId);
    }

    @Override
    public int hashCode(){
        return Objects.hash(userId, postId);
    }
}
