package com.Doctalk.Doctalk_backend.controller;

import com.Doctalk.Doctalk_backend.dto.reponse.DocumentReponse;
import com.Doctalk.Doctalk_backend.dto.request.DocumentRequest;
import com.Doctalk.Doctalk_backend.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<DocumentReponse> create(@RequestBody DocumentRequest request) {
        DocumentReponse response = documentService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentReponse> getById(@PathVariable Long id) {
        DocumentReponse response = documentService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentReponse>> getAll() {
        List<DocumentReponse> responses = documentService.getAll();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentReponse> update(@PathVariable Long id, @RequestBody DocumentRequest request) {
        DocumentReponse response = documentService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}