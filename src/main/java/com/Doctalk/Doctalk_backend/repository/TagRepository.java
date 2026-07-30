package com.Doctalk.Doctalk_backend.repository;

import com.Doctalk.Doctalk_backend.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag,Long> {
}
