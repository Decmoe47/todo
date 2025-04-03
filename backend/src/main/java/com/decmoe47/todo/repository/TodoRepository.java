package com.decmoe47.todo.repository;

import com.decmoe47.todo.model.entity.Todo;

import java.util.List;

public interface TodoRepository extends BaseRepository<Todo, Long> {

    List<Todo> findByCreatedByIdAndBelongedList_Id(long userId, String listId);

    void deleteByBelongedList_Id(String belongedListId);
}
