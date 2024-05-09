package com.naukma.thesisbackend.controllers;

import com.naukma.thesisbackend.entities.Tag;
import com.naukma.thesisbackend.services.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags(){
        return ResponseEntity.ok(
                tagService.getAllTags()
        );
    }

    @GetMapping("/{tagId}")
    public ResponseEntity<Tag> getTag(@PathVariable("tagId") Long tagId){
        return ResponseEntity.ok(tagService.get(tagId));
    }

    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestParam(required = true) String name){
        return ResponseEntity.ok(tagService.create(name));
    }

    @PatchMapping("/{tagId}")
    public ResponseEntity<Tag> updateTag(@PathVariable("tagId") Long tagId, @RequestParam(required = true) String name){
        return ResponseEntity.ok(tagService.update(tagId, name));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<?> deleteTag(@PathVariable("tagId") Long tagId){
        tagService.delete(tagId);
        return ResponseEntity.ok().build();
    }
}