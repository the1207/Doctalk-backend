package com.Doctalk.Doctalk_backend.service;

import com.Doctalk.Doctalk_backend.dto.reponse.FolderReponse;
import com.Doctalk.Doctalk_backend.dto.request.FolderRequest;

import java.util.List;

public interface FolderService {
    FolderReponse create(FolderRequest request);
    FolderReponse getById(Long id);
    List<FolderReponse> getAll();
    FolderReponse update(Long id, FolderRequest request);
    void delete(Long id);
}