package com.Doctalk.Doctalk_backend.Mapper;

import com.Doctalk.Doctalk_backend.dto.reponse.DocumentReponse;
import com.Doctalk.Doctalk_backend.dto.request.DocumentRequest;
import com.Doctalk.Doctalk_backend.entities.Document;
import com.Doctalk.Doctalk_backend.entities.Folder;
import com.Doctalk.Doctalk_backend.entities.Tag;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DocumentsMapper {

    public Document toEntity(DocumentRequest documentRequest) {
        Document document = new Document();
        document.setTitle(documentRequest.title());
        document.setFilename(documentRequest.filename());
        document.setFilePath(documentRequest.filePath());
        document.setFileSize(documentRequest.fileSize());
        document.setMimeType(documentRequest.mimeType());
        document.setContent(documentRequest.content());
        document.setStatus(documentRequest.status());
        return document;
    }

    public DocumentReponse toResponse(Document document) {
        Set<String> tagNames = document.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
        Set<String>folderNames = document.getFolders().stream()
                .map(Folder::getName)
                .collect(Collectors.toSet());
        return new DocumentReponse(
                document.getId(),
                document.getTitle(),
                document.getFilename(),
                document.getFilePath(),
                document.getFileSize(),
                document.getMimeType(),
                document.getContent(),
                document.getStatus(),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                tagNames,
                folderNames
        );
    }
}