package com.naukma.thesisbackend.entities.keys;

import com.naukma.thesisbackend.entities.User;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
public class CommentLikeKey implements Serializable {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "comment_id")
    private Long commentId;


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final CommentLikeKey other = (CommentLikeKey) obj;

        return this.userId.equals(other.userId)&&this.commentId.equals(other.commentId);
    }

    @Override
    public int hashCode(){
        return Objects.hash(userId, commentId);
    }
}
