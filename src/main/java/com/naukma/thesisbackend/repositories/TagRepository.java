package com.naukma.thesisbackend.repositories;

import com.naukma.thesisbackend.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findTagByNameIgnoreCase(String name);
    Optional<Tag> findTagByTagId(Long tagId);


}
