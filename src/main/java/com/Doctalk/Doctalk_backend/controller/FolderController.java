package com.Doctalk.Doctalk_backend.controller;

import com.Doctalk.Doctalk_backend.dto.reponse.FolderReponse;
import com.Doctalk.Doctalk_backend.dto.request.FolderRequest;
import com.Doctalk.Doctalk_backend.service.FolderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    public ResponseEntity<FolderReponse> create(@RequestBody FolderRequest request) {
        FolderReponse response = folderService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FolderReponse> getById(@PathVariable Long id) {
        FolderReponse response = folderService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<FolderReponse>> getAll() {
        List<FolderReponse> responses = folderService.getAll();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FolderReponse> update(@PathVariable Long id, @RequestBody FolderRequest request) {
        FolderReponse response = folderService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        folderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}