package com.decmoe47.todo.repository.impl

import com.decmoe47.todo.model.entity.TodoList
import com.decmoe47.todo.model.entity.todoList
import com.decmoe47.todo.repository.TodoListRepository
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.query.firstOrNull
import org.komapper.jdbc.JdbcDatabase
import org.springframework.stereotype.Component

@Component
class TodoListRepositoryKomapperImpl(private val db: JdbcDatabase) : TodoListRepository {

    private val tl = Meta.todoList

    override fun selectExcludingInbox(userId: Long): List<TodoList> = db.runQuery {
        QueryDsl.from(tl).where {
            tl.auditable.createdBy eq userId
            tl.inbox eq false
        }
    }

    override fun getInbox(userId: Long): TodoList = db.runQuery {
        QueryDsl.from(tl).where {
            tl.auditable.createdBy eq userId
            tl.inbox eq true
        }.first()
    }

    override fun first(id: Long): TodoList? = db.runQuery {
        QueryDsl.from(tl).where { tl.id eq id }.firstOrNull()
    }

    override fun save(todoList: TodoList): TodoList = db.runQuery {
        QueryDsl.insert(tl).single(todoList)
    }

    override fun update(todoList: TodoList): TodoList = db.runQuery {
        QueryDsl.update(tl).single(todoList)
    }

    override fun delete(id: Long) {
        db.runQuery {
            QueryDsl.delete(tl).where { tl.id eq id }
        }
    }
}
