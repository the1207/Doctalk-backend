package com.Doctalk.Doctalk_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Doctalk.Doctalk_backend.entities.Document;
import com.Doctalk.Doctalk_backend.entities.Folder;

public interface FolderRepository extends JpaRepository<Folder,Long> {
    @Query("SELECT d FROM Document d JOIN d.folders f WHERE f.id = :folderId")
    List<Document> findDocumentsByFolderId(@Param("folderId") Long folderId);
}
