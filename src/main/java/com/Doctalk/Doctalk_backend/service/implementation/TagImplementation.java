package com.Doctalk.Doctalk_backend.service.implementation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.Doctalk.Doctalk_backend.Exeption.TagNotFoundException;
import com.Doctalk.Doctalk_backend.Mapper.TagMapper;
import com.Doctalk.Doctalk_backend.dto.reponse.TagReponse;
import com.Doctalk.Doctalk_backend.dto.request.TagRequest;
import com.Doctalk.Doctalk_backend.entities.Document;
import com.Doctalk.Doctalk_backend.entities.Tag;
import com.Doctalk.Doctalk_backend.repository.DocumentsRepository;
import com.Doctalk.Doctalk_backend.repository.TagRepository;
import com.Doctalk.Doctalk_backend.service.TagService;

import jakarta.transaction.Transactional;

@Service
public class TagImplementation implements TagService {

    private final TagRepository tagRepository;
    private final DocumentsRepository documentsRepository;
    private final TagMapper tagMapper;

    public TagImplementation(TagRepository tagRepository, DocumentsRepository documentsRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
        this.documentsRepository = documentsRepository;
        this.tagMapper = tagMapper;
    }

    @Override
    public TagReponse create(TagRequest request) {
        Tag tag = tagMapper.toEntity(request);
        tag = tagRepository.save(tag);
        return tagMapper.toResponse(tag);
    }

    @Override
    public TagReponse getById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));
        return tagMapper.toResponse(tag);
    }

    @Override
    public List<TagReponse> getAll() {
        return tagRepository.findAll()
                .stream()
                .map(tagMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TagReponse update(Long id, TagRequest request) {
        Tag existingTag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));

        if (request.name() != null) existingTag.setName(request.name());
        if (request.color() != null) existingTag.setColor(request.color());

        Tag updatedTag = tagRepository.save(existingTag);
        return tagMapper.toResponse(updatedTag);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));

        List<Document> docs = tagRepository.findDocumentsByTagId(id);
        docs.forEach(d -> d.getTags().remove(tag));
        documentsRepository.saveAll(docs);

        tagRepository.delete(tag);
    }
}