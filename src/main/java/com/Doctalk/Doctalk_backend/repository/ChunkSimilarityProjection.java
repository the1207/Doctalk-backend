package com.Doctalk.Doctalk_backend.repository;

public interface ChunkSimilarityProjection {
    Long getId();
    String getContent();
    Integer getChunkIndex();
    Long getDocumentId();
    String getDocumentTitle();
    Double getSimilarity();
}
