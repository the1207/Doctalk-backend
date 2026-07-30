package com.Doctalk.Doctalk_backend.repository;

import com.Doctalk.Doctalk_backend.entities.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder,Long> {
}
