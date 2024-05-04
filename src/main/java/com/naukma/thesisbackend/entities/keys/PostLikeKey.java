package com.naukma.thesisbackend.entities.keys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class PostLikeKey implements Serializable {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "post_id")
    private Long postId;
}
