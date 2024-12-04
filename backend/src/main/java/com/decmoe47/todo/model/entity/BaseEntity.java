package com.decmoe47.todo.model.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@MappedSuperclass
public abstract class BaseEntity {

    protected Boolean deleted;
}
