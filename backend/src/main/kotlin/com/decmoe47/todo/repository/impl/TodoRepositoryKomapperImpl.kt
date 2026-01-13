package com.decmoe47.todo.repository.impl

import com.decmoe47.todo.model.entity.Todo
import com.decmoe47.todo.model.entity.todo
import com.decmoe47.todo.repository.TodoRepository
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.firstOrNull
import org.komapper.jdbc.JdbcDatabase
import org.springframework.stereotype.Component

@Component
class TodoRepositoryKomapperImpl(private val db: JdbcDatabase) : TodoRepository {

    private val t = Meta.todo

    override fun select(listId: Long): List<Todo> = db.runQuery {
        QueryDsl.from(t).where {
            t.belongedListId eq listId
        }
    }

    override fun first(id: Long): Todo? = db.runQuery {
        QueryDsl.from(t).where { t.id eq id }.firstOrNull()
    }

    override fun selectIn(ids: List<Long>): List<Todo> {
        if (ids.isEmpty()) return emptyList()
        return db.runQuery {
            QueryDsl.from(t).where { t.id inList ids }
        }
    }

    override fun save(todo: Todo): Todo = db.runQuery {
        QueryDsl.insert(t).single(todo)
    }

    override fun update(todo: Todo): Todo = db.runQuery {
        QueryDsl.update(t).single(todo)
    }

    override fun delete(id: Long) {
        db.runQuery {
            QueryDsl.delete(t).where { t.id eq id }
        }
    }

    override fun deleteByBelongedListId(belongedListId: Long) {
        db.runQuery {
            QueryDsl.delete(t).where { t.belongedListId eq belongedListId }
        }
    }

    override fun softDelete(id: Long) {
        db.runQuery {
            QueryDsl.update(t).set { t.auditable.deleted eq true }.where { t.id eq id }
        }
    }
}
