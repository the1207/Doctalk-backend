package com.Doctalk.Doctalk_backend.repository;

import com.Doctalk.Doctalk_backend.entities.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChunkRepository extends JpaRepository<Chunk, Long> {

    List<Chunk> findByDocumentIdOrderByChunkIndexAsc(Long documentId);

    void deleteByDocumentId(Long documentId);
    @Query(value = "SELECT c.* FROM chunks c " +
            "WHERE document_id = :documentId " +
            "ORDER BY 1 - (c.embedding::vector <=> cast(:queryEmbedding as vector)) DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Chunk> findSimilarChunks(@Param("documentId") Long documentId,
                                  @Param("queryEmbedding") String queryEmbedding,
                                  @Param("limit") int limit);

    @Query(value = "SELECT c.* FROM chunks c " +
            "ORDER BY 1 - (c.embedding::vector <=> cast(:queryEmbedding as vector)) DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Chunk> findSimilarChunksAll(@Param("queryEmbedding") String queryEmbedding,
                                     @Param("limit") int limit);

}
