package com.decmoe47.todo.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Accessors(chain = true)
@Data
@SQLRestriction("deleted = false")
@DynamicUpdate
@MappedSuperclass
public abstract class BaseEntity<T> {

    @CreatedDate
    protected LocalDateTime createTime;
    @CreatedBy
    protected String createdBy;
    @LastModifiedDate
    protected LocalDateTime updateTime;
    @LastModifiedBy
    protected String updatedBy;
    protected Boolean deleted;
    protected LocalDateTime deletedTime;
    @Version
    protected int version;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private T id;
}