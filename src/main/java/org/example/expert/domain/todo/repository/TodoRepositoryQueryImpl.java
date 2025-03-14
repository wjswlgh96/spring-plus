package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.TodoQueryResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class TodoRepositoryQueryImpl implements TodoRepositoryQuery {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable, String weather, LocalDateTime startDate, LocalDateTime endDate) {

        BooleanBuilder builder = new BooleanBuilder();
        if (weather != null && !weather.isEmpty()) {
            builder.and(todo.weather.containsIgnoreCase(weather));
        }

        if (startDate != null && endDate != null) {
            builder.and(todo.modifiedAt.between(startDate, endDate));
        } else if (startDate != null) {
            builder.and(todo.modifiedAt.goe(startDate));
        } else if (endDate != null) {
            builder.and(todo.modifiedAt.loe(endDate));
        }

        List<Todo> response = jpaQueryFactory.select(todo)
            .from(todo)
            .join(todo.user).fetchJoin()
            .where(builder)
            .orderBy(todo.modifiedAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = Optional.ofNullable(
            jpaQueryFactory.select(todo.count())
                .from(todo)
                .where(builder)
                .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(response, pageable, total);
    }

    @Override
    public Page<TodoQueryResponse> findAllByOrderByCreatedAtDesc(Pageable pageable, String search, LocalDateTime startDate, LocalDateTime endDate) {
        BooleanBuilder builder = new BooleanBuilder();

        if (search != null && !search.isEmpty()) {
            builder.and(todo.title.contains(search)
                .or(user.nickname.contains(search)));
        }

        if (startDate != null && endDate != null) {
            builder.and(todo.createdAt.between(startDate, endDate));
        } else if (startDate != null) {
            builder.and(todo.createdAt.goe(startDate));
        } else if (endDate != null) {
            builder.and(todo.createdAt.loe(endDate));
        }

        List<TodoQueryResponse> response = jpaQueryFactory
            .selectDistinct(
                Projections.constructor(TodoQueryResponse.class,
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
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = Optional.ofNullable(
            jpaQueryFactory.select(todo.count())
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .where(builder)
                .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(response, pageable, total);
    }

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        return Optional.ofNullable(jpaQueryFactory.select(todo)
            .from(todo)
            .leftJoin(todo.user)
            .fetchJoin()
            .where(todo.id.eq(todoId))
            .fetchOne()
        );
    }
}
