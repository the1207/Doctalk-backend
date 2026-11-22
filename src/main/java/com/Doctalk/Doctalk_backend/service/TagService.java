package com.Doctalk.Doctalk_backend.service;

import com.Doctalk.Doctalk_backend.dto.reponse.TagReponse;
import com.Doctalk.Doctalk_backend.dto.request.TagRequest;

import java.util.List;

public interface TagService {
    TagReponse create(TagRequest request);
    TagReponse getById(Long id);
    List<TagReponse> getAll();
    TagReponse update(Long id, TagRequest request);
    void delete(Long id);
}