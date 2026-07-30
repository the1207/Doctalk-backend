package com.Doctalk.Doctalk_backend.Mapper;

import com.Doctalk.Doctalk_backend.dto.reponse.TagReponse;
import com.Doctalk.Doctalk_backend.dto.request.TagRequest;
import com.Doctalk.Doctalk_backend.entities.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public Tag toEntity(TagRequest request) {
        Tag tag = new Tag();
        tag.setName(request.name());
        tag.setColor(request.color());
        return tag;
    }

    public TagReponse toResponse(Tag tag) {
        return new TagReponse(
                tag.getId(),
                tag.getName(),
                tag.getColor(),
                tag.getCreatedAt(),
                tag.getUpdatedAt()
        );
    }
}