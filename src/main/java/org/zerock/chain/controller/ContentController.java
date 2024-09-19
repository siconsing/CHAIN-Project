package org.zerock.chain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zerock.chain.model.Content;
import org.zerock.chain.repository.ContentRepository;

import java.util.Optional;

@RestController
@RequestMapping("/content")
public class ContentController {

    @Autowired
    private ContentRepository contentRepository;

    @PostMapping("/save")
    public ResponseEntity<?> saveContent(@RequestBody Content content) {
        Content savedContent = contentRepository.save(content);
        return new ResponseEntity<>(savedContent, HttpStatus.OK);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getContent(@PathVariable Long id) {
        Optional<Content> content = contentRepository.findById(id);
        if (content.isPresent()) {
            return new ResponseEntity<>(content.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Content not found", HttpStatus.NOT_FOUND);
        }
    }
}