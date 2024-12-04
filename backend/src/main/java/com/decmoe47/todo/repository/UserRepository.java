package com.decmoe47.todo.repository;

import com.decmoe47.todo.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<List<User>> findByEmailContaining(String email);

    Optional<List<User>> findByNameContaining(String username);
}