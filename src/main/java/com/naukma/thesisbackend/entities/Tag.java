package com.naukma.thesisbackend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Tag of post. Each post can have several tags
 */
@Getter
@Setter
@Entity
@Table(name = "tag")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    /**
     * string representation of tag
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * set of posts, tagged by this tag
     */
    @JsonIgnore
    @ManyToMany(mappedBy = "tags")
    private Set<Post> posts;
}
