package com.Doctalk.Doctalk_backend.service.implementation;

import com.Doctalk.Doctalk_backend.dto.reponse.FolderReponse;
import com.Doctalk.Doctalk_backend.dto.request.FolderRequest;
import com.Doctalk.Doctalk_backend.entities.Folder;
import com.Doctalk.Doctalk_backend.Mapper.FolderMapper;
import com.Doctalk.Doctalk_backend.repository.FolderRepository;
import com.Doctalk.Doctalk_backend.service.FolderService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FolderImplementation implements FolderService {

    private final FolderRepository folderRepository;
    private final FolderMapper folderMapper;

    public FolderImplementation(FolderRepository folderRepository, FolderMapper folderMapper) {
        this.folderRepository = folderRepository;
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
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'id : " + id));
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
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'id : " + id));

        existingFolder.setName(request.name());
        existingFolder.setParentId(request.parentId());

        Folder updatedFolder = folderRepository.save(existingFolder);
        return folderMapper.toResponse(updatedFolder);
    }

    @Override
    public void delete(Long id) {
        if (!folderRepository.existsById(id)) {
            throw new RuntimeException("Dossier non trouvé avec l'id : " + id);
        }
        folderRepository.deleteById(id);
    }
}