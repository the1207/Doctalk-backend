package com.Doctalk.Doctalk_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.Doctalk.Doctalk_backend.dto.reponse.DocumentReponse;
import com.Doctalk.Doctalk_backend.dto.request.DocumentRequest;
import com.Doctalk.Doctalk_backend.service.DocumentService;
import com.Doctalk.Doctalk_backend.service.FileStorageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;

    // Un seul constructeur : Spring peut faire l'injection sans ambiguïté
    public DocumentController(DocumentService documentService, FileStorageService fileStorageService) {
        this.documentService = documentService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping
    public ResponseEntity<DocumentReponse> create(@Valid @RequestBody DocumentRequest request) {
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
    public ResponseEntity<DocumentReponse> update(@PathVariable Long id, @Valid @RequestBody DocumentRequest request) {
        DocumentReponse response = documentService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentReponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("status") String status) {

        // 1. Sauvegarde physique du fichier
        String storedFilename = fileStorageService.storeFile(file);

        // 2. Construction de la requête pour le service
        DocumentRequest request = new DocumentRequest(
                title,
                file.getOriginalFilename(),
                storedFilename,
                file.getSize(),
                file.getContentType(),
                null, // content sera extrait par le parsing
                status
        );

        // 3. Sauvegarde + parsing PDF + chunking + embeddings
        DocumentReponse response = documentService.createWithParsing(request, file);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Endpoints pour les Tags

    @PostMapping("/{documentId}/tags/{tagId}")
    public ResponseEntity<DocumentReponse> addTagToDocument(
            @PathVariable Long documentId,
            @PathVariable Long tagId) {
        DocumentReponse response = documentService.addTagToDocument(documentId, tagId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{documentId}/tags/{tagId}")
    public ResponseEntity<Void> removeTagFromDocument(
            @PathVariable Long documentId,
            @PathVariable Long tagId) {
        documentService.removeTagFromDocument(documentId, tagId);
        return ResponseEntity.noContent().build();
    }

    // Endpoints pour les Folders

    @PostMapping("/{documentId}/folders/{folderId}")
    public ResponseEntity<DocumentReponse> addFolderToDocument(
            @PathVariable Long documentId,
            @PathVariable Long folderId) {
        DocumentReponse response = documentService.addFolderToDocument(documentId, folderId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{documentId}/folders/{folderId}")
    public ResponseEntity<Void> removeFolderFromDocument(
            @PathVariable Long documentId,
            @PathVariable Long folderId) {
        documentService.removeFolderFromDocument(documentId, folderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<DocumentReponse>> searchDocuments(
            @RequestParam("q") String keyword) {
        List<DocumentReponse> results = documentService.searchByKeyword(keyword);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/search/semantic")
    public ResponseEntity<List<DocumentReponse>> searchSemantic(
            @RequestParam("q") String query) {
        List<DocumentReponse> results = documentService.searchSemantic(query);
        return ResponseEntity.ok(results);
    }
}