package com.Doctalk.Doctalk_backend.service.implementation;

import com.Doctalk.Doctalk_backend.dto.reponse.DocumentReponse;
import com.Doctalk.Doctalk_backend.dto.request.DocumentRequest;
import com.Doctalk.Doctalk_backend.entities.Document;
import com.Doctalk.Doctalk_backend.Mapper.DocumentsMapper;
import com.Doctalk.Doctalk_backend.repository.DocumentsRepository;
import com.Doctalk.Doctalk_backend.service.DocumentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentImplementation implements DocumentService {

    private final DocumentsRepository documentsRepository;
    private final DocumentsMapper documentsMapper;

    public DocumentImplementation(DocumentsRepository documentsRepository, DocumentsMapper documentsMapper) {
        this.documentsRepository = documentsRepository;
        this.documentsMapper = documentsMapper;
    }

    @Override
    public DocumentReponse create(DocumentRequest request) {
        Document document = documentsMapper.toEntity(request);
        document = documentsRepository.save(document);
        return documentsMapper.toResponse(document);
    }

    @Override
    public DocumentReponse getById(Long id) {
        Document document = documentsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé avec l'id : " + id));
        return documentsMapper.toResponse(document);
    }

    @Override
    public List<DocumentReponse> getAll() {
        return documentsRepository.findAll()
                .stream()
                .map(documentsMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentReponse update(Long id, DocumentRequest request) {
        Document existingDocument = documentsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé avec l'id : " + id));

        existingDocument.setTitle(request.title());
        existingDocument.setFilename(request.filename());
        existingDocument.setFilePath(request.filePath());
        existingDocument.setFileSize(request.fileSize());
        existingDocument.setMimeType(request.mimeType());
        existingDocument.setContent(request.content());
        existingDocument.setStatus(request.status());

        Document updatedDocument = documentsRepository.save(existingDocument);
        return documentsMapper.toResponse(updatedDocument);
    }

    @Override
    public void delete(Long id) {
        if (!documentsRepository.existsById(id)) {
            throw new RuntimeException("Document non trouvé avec l'id : " + id);
        }
        documentsRepository.deleteById(id);
    }
}