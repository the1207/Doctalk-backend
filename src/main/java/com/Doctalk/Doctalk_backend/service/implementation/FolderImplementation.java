package com.Doctalk.Doctalk_backend.service.implementation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.Doctalk.Doctalk_backend.Exeption.FolderNotFoundException;
import com.Doctalk.Doctalk_backend.Mapper.FolderMapper;
import com.Doctalk.Doctalk_backend.dto.reponse.FolderReponse;
import com.Doctalk.Doctalk_backend.dto.request.FolderRequest;
import com.Doctalk.Doctalk_backend.entities.Document;
import com.Doctalk.Doctalk_backend.entities.Folder;
import com.Doctalk.Doctalk_backend.repository.DocumentsRepository;
import com.Doctalk.Doctalk_backend.repository.FolderRepository;
import com.Doctalk.Doctalk_backend.service.FolderService;

import jakarta.transaction.Transactional;

@Service
public class FolderImplementation implements FolderService {

    private final FolderRepository folderRepository;
    private final DocumentsRepository documentsRepository;
    private final FolderMapper folderMapper;

    public FolderImplementation(FolderRepository folderRepository, DocumentsRepository documentsRepository, FolderMapper folderMapper) {
        this.folderRepository = folderRepository;
        this.documentsRepository = documentsRepository;
        this.folderMapper = folderMapper;
    }

    @Override
    public FolderReponse create(FolderRequest request) {
        Folder folder = folderMapper.toEntity(request);
        folder = folderRepository.save(folder);
        return folderMapper.toResponse(folder);
    }

    @Override
    public FolderReponse getById(Long id) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new FolderNotFoundException(id));
        return folderMapper.toResponse(folder);
    }

    @Override
    public List<FolderReponse> getAll() {
        return folderRepository.findAll()
                .stream()
                .map(folderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public FolderReponse update(Long id, FolderRequest request) {
        Folder existingFolder = folderRepository.findById(id)
                .orElseThrow(() -> new FolderNotFoundException(id));

        if (request.name() != null) existingFolder.setName(request.name());
        if (request.parentId() != null) existingFolder.setParentId(request.parentId());

        Folder updatedFolder = folderRepository.save(existingFolder);
        return folderMapper.toResponse(updatedFolder);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new FolderNotFoundException(id));

        List<Document> docs = folderRepository.findDocumentsByFolderId(id);
        docs.forEach(d -> d.getFolders().remove(folder));
        documentsRepository.saveAll(docs);

        folderRepository.delete(folder);
    }
}