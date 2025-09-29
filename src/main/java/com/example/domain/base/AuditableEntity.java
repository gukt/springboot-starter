package com.example.domain.base;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@MappedSuperclass
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity<T extends Serializable> extends BaseEntity<T> {

    @CreatedDate
    @Column(nullable = false, columnDefinition = "datetime default current_timestamp")
    protected LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false, columnDefinition = "datetime default current_timestamp")
    protected LocalDateTime updatedAt;
}