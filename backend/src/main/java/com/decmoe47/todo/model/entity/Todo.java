package com.decmoe47.todo.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@Entity
public class Todo extends Auditable<User> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String content;
    private LocalDateTime dueDate;
    private boolean done = false;
    private String description;

    @ManyToOne
    @JoinColumn(name = "belonged_list_id")
    private TodoList belongedList;
}
