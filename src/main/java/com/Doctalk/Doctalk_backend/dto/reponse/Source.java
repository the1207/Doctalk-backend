package com.Doctalk.Doctalk_backend.dto.reponse;
public record Source(
        Long chunkId,
        String content,
        String documentTitle,
        Double similarity
) {}