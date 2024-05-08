package com.naukma.thesisbackend.repositories;

import com.naukma.thesisbackend.entities.*;
import com.naukma.thesisbackend.entities.keys.CommentLikeKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeKey> {
    Optional<CommentLike> findByUserAndComment(User user, Comment comment);
}