package com.Doctalk.Doctalk_backend.service.implementation;

import com.Doctalk.Doctalk_backend.dto.reponse.TagReponse;
import com.Doctalk.Doctalk_backend.dto.request.TagRequest;
import com.Doctalk.Doctalk_backend.entities.Tag;
import com.Doctalk.Doctalk_backend.Mapper.TagMapper;
import com.Doctalk.Doctalk_backend.repository.TagRepository;
import com.Doctalk.Doctalk_backend.service.TagService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagImplementation implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagImplementation(TagRepository tagRepository, TagMapper tagMapper) {
        this.tagRepository = tagRepository;
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
                .orElseThrow(() -> new RuntimeException("Tag non trouvé avec l'id : " + id));
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
                .orElseThrow(() -> new RuntimeException("Tag non trouvé avec l'id : " + id));

        existingTag.setName(request.name());
        existingTag.setColor(request.color());

        Tag updatedTag = tagRepository.save(existingTag);
        return tagMapper.toResponse(updatedTag);
    }

    @Override
    public void delete(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new RuntimeException("Tag non trouvé avec l'id : " + id);
        }
        tagRepository.deleteById(id);
    }
}