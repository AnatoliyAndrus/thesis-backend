package com.naukma.thesisbackend.repositories;

import com.naukma.thesisbackend.entities.Post;
import com.naukma.thesisbackend.entities.PostLike;
import com.naukma.thesisbackend.entities.User;
import com.naukma.thesisbackend.entities.keys.PostLikeKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLikeKey> {
    Optional<PostLike> findByUserAndPost(User user, Post post);

}