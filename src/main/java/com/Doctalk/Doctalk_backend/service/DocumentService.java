package com.Doctalk.Doctalk_backend.service;

import com.Doctalk.Doctalk_backend.dto.reponse.DocumentReponse;
import com.Doctalk.Doctalk_backend.dto.request.DocumentRequest;

import java.util.List;

public interface DocumentService {
    DocumentReponse create(DocumentRequest request);
    DocumentReponse getById(Long id);
    List<DocumentReponse> getAll();
    DocumentReponse update(Long id, DocumentRequest request);
    void delete(Long id);
}