package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.todo.entity.QTodo.todo;

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
}
