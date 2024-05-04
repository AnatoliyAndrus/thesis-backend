package com.naukma.thesisbackend.entities.keys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class CommentLikeKey implements Serializable {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "comment_id")
    private Long commentId;
}
