package com.decmoe47.todo.repository;

import com.decmoe47.todo.model.entity.BaseEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BaseRepository<T extends BaseEntity<D>, D> extends JpaRepository<T, D> {

    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = false, e.delete_time = now() WHERE e.id = ?1")
    void softDeleteById(@NotNull D id);

    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = false, e.delete_time = now() WHERE e.id in ?1")
    void softDeleteAllById(Iterable<D> idList);
}
