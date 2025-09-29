package com.example.domain.base;

import jakarta.persistence.Column;
import jakarta.persistence.Transient;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 软删除实体基类
 */
@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SQLRestriction("deleted = false")
public abstract class SoftDeletableEntity<T extends Serializable> extends BaseEntity<T>  {

    @Column(nullable = false, columnDefinition = "boolean default 0")
    private Boolean deleted = false;

    @Column(nullable = false, columnDefinition = "datetime default current_timestamp")
    private LocalDateTime deletedAt;

    private String deletedBy;

    @Transient
    public void markDelete(String deletedBy) {
        this.setDeleted(true);
        this.setDeletedAt(LocalDateTime.now());
        this.setDeletedBy(deletedBy);
    }

    @Transient
    public void markDelete() {
        String currentUser = "anonymousUser"; // Default value
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        }
        this.markDelete(currentUser);
    }

    @Transient
    public void restore() {
        this.setDeleted(false);
        this.setDeletedAt(null);
        this.setDeletedBy(null);
    }
}

