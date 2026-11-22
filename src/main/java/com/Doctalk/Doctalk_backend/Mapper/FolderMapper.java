package com.Doctalk.Doctalk_backend.Mapper;

import com.Doctalk.Doctalk_backend.dto.reponse.FolderReponse;
import com.Doctalk.Doctalk_backend.dto.request.FolderRequest;
import com.Doctalk.Doctalk_backend.entities.Folder;
import org.springframework.stereotype.Component;

@Component
public class FolderMapper {

    public Folder toEntity(FolderRequest request) {
        Folder folder = new Folder();
        folder.setName(request.name());
        folder.setParentId(request.parentId());
        return folder;
    }

    public FolderReponse toResponse(Folder folder) {
        return new FolderReponse(
                folder.getId(),
                folder.getName(),
                folder.getParentId(),
                folder.getCreatedAt(),
                folder.getUpdatedAt()
        );
    }
}