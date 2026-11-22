package com.Doctalk.Doctalk_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Doctalk.Doctalk_backend.entities.Document;
import com.Doctalk.Doctalk_backend.entities.Tag;

public interface TagRepository extends JpaRepository<Tag,Long> {
    @Query("SELECT d FROM Document d JOIN d.tags t WHERE t.id = :tagId")
    List<Document> findDocumentsByTagId(@Param("tagId") Long tagId);
}
