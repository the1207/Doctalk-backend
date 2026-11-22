package com.Doctalk.Doctalk_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Doctalk.Doctalk_backend.entities.Chunk;

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

    @Query(value = "SELECT c.id as id, c.content as content, c.chunk_index as chunkIndex, " +
            "c.document_id as documentId, d.title as documentTitle, " +
            "1 - (c.embedding::vector <=> cast(:queryEmbedding as vector)) as similarity " +
            "FROM chunks c JOIN document d ON d.id = c.document_id " +
            "WHERE c.document_id = :documentId " +
            "ORDER BY similarity DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<ChunkSimilarityProjection> findSimilarChunksWithScore(@Param("documentId") Long documentId,
                                                                @Param("queryEmbedding") String queryEmbedding,
                                                                @Param("limit") int limit);

    @Query(value = "SELECT c.id as id, c.content as content, c.chunk_index as chunkIndex, " +
            "c.document_id as documentId, d.title as documentTitle, " +
            "1 - (c.embedding::vector <=> cast(:queryEmbedding as vector)) as similarity " +
            "FROM chunks c JOIN document d ON d.id = c.document_id " +
            "ORDER BY similarity DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<ChunkSimilarityProjection> findSimilarChunksAllWithScore(@Param("queryEmbedding") String queryEmbedding,
                                                                   @Param("limit") int limit);

}
