package com.decmoe47.todo.repository.impl

import com.decmoe47.todo.model.entity.User
import com.decmoe47.todo.model.entity.user
import com.decmoe47.todo.repository.UserRepository
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.firstOrNull
import org.komapper.jdbc.JdbcDatabase
import org.springframework.stereotype.Component

@Component
class UserRepositoryKomapperImpl(private val db: JdbcDatabase) : UserRepository {

    private val u = Meta.user

    override fun first(id: Long): User? = db.runQuery {
        QueryDsl.from(u).where { u.id eq id }.firstOrNull()
    }

    override fun firstByEmail(email: String): User? = db.runQuery {
        QueryDsl.from(u).where { u.email eq email }.firstOrNull()
    }

    override fun selectByEmail(email: String): List<User> = db.runQuery {
        QueryDsl.from(u).where { u.email eq email }
    }

    override fun selectByName(username: String): List<User> = db.runQuery {
        QueryDsl.from(u).where { u.name eq username }
    }

    override fun save(user: User): User = db.runQuery {
        QueryDsl.insert(u).single(user)
    }

    override fun update(user: User): User = db.runQuery {
        QueryDsl.update(u).single(user)
    }
}
