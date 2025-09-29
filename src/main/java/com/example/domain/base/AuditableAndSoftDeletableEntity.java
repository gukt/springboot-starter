package com.example.domain.base;

import jakarta.persistence.EntityListeners;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.mapping.SoftDeletable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审计软删除实体类
 *
 * 结合审计功能和软删除功能，通过接口组合消除代码重复
 */
@MappedSuperclass
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SQLRestriction("deleted = false")
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableAndSoftDeletableEntity<T extends Serializable> extends SoftDeletableEntity <T> {

    @CreatedDate
    @Column(nullable = false, columnDefinition = "datetime default current_timestamp")
    protected LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false, columnDefinition = "datetime default current_timestamp")
    protected LocalDateTime updatedAt;
}