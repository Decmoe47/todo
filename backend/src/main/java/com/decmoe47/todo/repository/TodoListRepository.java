package com.decmoe47.todo.repository;

import com.decmoe47.todo.model.entity.TodoList;

import java.util.List;

public interface TodoListRepository extends BaseRepository<TodoList, String> {

    List<TodoList> findByCreatedById(long userId);
}
