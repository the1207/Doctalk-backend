package com.Doctalk.Doctalk_backend.service;

import com.Doctalk.Doctalk_backend.dto.reponse.DocumentReponse;
import com.Doctalk.Doctalk_backend.dto.request.DocumentRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    DocumentReponse create(DocumentRequest request);
    DocumentReponse createWithParsing(DocumentRequest request, MultipartFile file);
    DocumentReponse getById(Long id);
    DocumentReponse update(Long id, DocumentRequest documentRequest);
    List<DocumentReponse> getAll();
    DocumentReponse addTagToDocument(Long documentId, Long tagId);
    DocumentReponse removeTagFromDocument(Long documentId, Long tagId);

    DocumentReponse addFolderToDocument(Long documentId, Long folderId);
    DocumentReponse removeFolderFromDocument(Long documentId, Long folderId);
    void delete(Long id);
    List<DocumentReponse> searchByKeyword(String keyword);
    List<DocumentReponse> searchSemantic(String query);
}