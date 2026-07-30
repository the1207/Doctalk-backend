package com.Doctalk.Doctalk_backend.repository;

import com.Doctalk.Doctalk_backend.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentsRepository extends JpaRepository<Document,Long>{
}
