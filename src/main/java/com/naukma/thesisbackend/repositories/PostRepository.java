package com.naukma.thesisbackend.repositories;

import com.naukma.thesisbackend.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findPostByPostId(Long postId);


    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN p.tags t " +
            "WHERE (:authorId IS NULL OR p.postAuthor.userId LIKE %:authorId%) " +
            "AND (:tagIds IS NULL OR t.tagId IN :tagIds) " +
            "AND (:minDate IS NULL OR p.postedDate >= :minDate) " +
            "AND (:maxDate IS NULL OR p.postedDate <= :maxDate) " +
            "AND (:title IS NULL OR p.title LIKE %:title%) ")
    Page<Post> findFilteredPosts(@Param("authorId") Long authorId,
                                 @Param("tagIds") List<Long> tagIds,
                                 @Param("minDate") LocalDateTime minDate,
                                 @Param("maxDate") LocalDateTime maxDate,
                                 @Param("title") String title,
                                 Pageable pageable);
}
