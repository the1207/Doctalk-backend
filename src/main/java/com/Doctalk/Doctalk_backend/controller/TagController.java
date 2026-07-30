package com.Doctalk.Doctalk_backend.controller;

import com.Doctalk.Doctalk_backend.dto.reponse.TagReponse;
import com.Doctalk.Doctalk_backend.dto.request.TagRequest;
import com.Doctalk.Doctalk_backend.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public ResponseEntity<TagReponse> create(@RequestBody TagRequest request) {
        TagReponse response = tagService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagReponse> getById(@PathVariable Long id) {
        TagReponse response = tagService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TagReponse>> getAll() {
        List<TagReponse> responses = tagService.getAll();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagReponse> update(@PathVariable Long id, @RequestBody TagRequest request) {
        TagReponse response = tagService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}