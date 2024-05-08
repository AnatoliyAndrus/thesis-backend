package com.naukma.thesisbackend.services;

import com.naukma.thesisbackend.entities.Tag;
import com.naukma.thesisbackend.repositories.TagRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public Tag get(Long tagId){
        return tagRepository
                .findTagByTagId(tagId)
                .orElseThrow(()->new EntityNotFoundException("No tag with such id present"));
    }
    public Tag create(String tagName){
        Tag tag = new Tag();
        tag.setName(tagName);

        return tagRepository.save(tag);
    }

    public Tag update(Long tagId, String newTagName){
        Tag tag = tagRepository
                .findTagByTagId(tagId)
                .orElseThrow(() -> new EntityNotFoundException("No such tag"));

        tag.setName(newTagName);
        return tagRepository.save(tag);
    }

    public void delete(Long tagId){
        tagRepository.deleteById(tagId);
    }
}
