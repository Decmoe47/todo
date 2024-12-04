package com.decmoe47.todo.repository;

import com.decmoe47.todo.model.entity.BaseEntity;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity, D> extends JpaRepository<T, D> {

    @Transactional
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = true WHERE e.id = ?1")
    void softDeleteById(@NotNull D id);

    @Transactional
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = true WHERE e.id in ?1")
    void softDeleteAllById(Iterable<D> idList);
}
