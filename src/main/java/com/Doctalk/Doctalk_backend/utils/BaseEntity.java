package com.Doctalk.Doctalk_backend.utils;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public abstract class BaseEntity {


    @Column(name = "date_creation",nullable = false,updatable = false)
    @CreatedDate
    private Instant createDate;

    @Column(name = "date_modification")
    @LastModifiedDate
    private Instant dateM;

    @CreatedBy
    private String userCreate;

    @LastModifiedBy
    private String userUpdate;




}
