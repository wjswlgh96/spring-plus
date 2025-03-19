package org.example.expert.domain.todo.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.example.expert.domain.manager.entity.QManager.manager
import org.example.expert.domain.todo.dto.response.TodoQueryResponse
import org.example.expert.domain.todo.entity.QTodo.todo
import org.example.expert.domain.todo.entity.Todo
import org.example.expert.domain.user.entity.QUser.user
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import java.time.LocalDateTime

class TodoRepositoryQueryImpl(
    private val queryFactory: JPAQueryFactory
): TodoRepositoryQuery {

    override fun findAllByOrderByModifiedAtDesc(
        pageable: Pageable,
        weather: String?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?
    ): Page<Todo> {
        val builder = BooleanBuilder()

        if (!weather.isNullOrEmpty()) {
            builder.and(todo.weather.containsIgnoreCase(weather))
        }

        applyModifiedAtRange(builder, startDate, endDate)

        val result: MutableList<Todo> = queryFactory
            .selectFrom(todo)
            .join(todo.user).fetchJoin()
            .where(builder)
            .orderBy(todo.modifiedAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val countQuery: JPAQuery<Long> = queryFactory
            .select(todo.count())
            .from(todo)
            .join(todo.user)
            .where(builder)


        return PageableExecutionUtils.getPage(result, pageable) {
            countQuery.fetchOne() ?: 0L
        }
    }

    override fun findAllByOrderByCreatedAtDesc(
        pageable: Pageable,
        search: String?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?
    ): Page<TodoQueryResponse> {
        val builder = BooleanBuilder()

        if (!search.isNullOrEmpty()) {
            builder.and(todo.title.contains(search)
                .or(user.nickname.contains(search)))
        }

        applyModifiedAtRange(builder, startDate, endDate)

        val result: MutableList<TodoQueryResponse> = queryFactory
            .selectDistinct(
                Projections.constructor(
                    TodoQueryResponse::class.java,
                    todo.title,
                    todo.managers.size(),
                    todo.comments.size(),
                    todo.createdAt
                )
            )
            .from(todo)
            .where(builder)
            .leftJoin(todo.managers, manager)
            .leftJoin(manager.user, user)
            .orderBy(todo.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val countQuery: JPAQuery<Long> = queryFactory
            .select(todo.count())
            .from(todo)
            .leftJoin(todo.managers, manager)
            .leftJoin(manager.user, user)
            .where(builder)

        return PageableExecutionUtils.getPage(result, pageable) {
            countQuery.fetchOne() ?: 0L
        }
    }

    override fun findByIdWithUser(todoId: Long): Todo? {
        return queryFactory
            .selectFrom(todo)
            .leftJoin(todo.user).fetchJoin()
            .where(todo.id.eq(todoId))
            .fetchOne()
    }

    private fun applyModifiedAtRange(builder: BooleanBuilder, startDate: LocalDateTime?, endDate: LocalDateTime?) {
        if (startDate != null && endDate != null) {
            builder.and(todo.modifiedAt.between(startDate, endDate))
        } else if (startDate != null) {
            builder.and(todo.modifiedAt.goe(startDate))
        } else if (endDate != null) {
            builder.and(todo.modifiedAt.loe(endDate))
        }
    }

}