package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.response.TodoQueryResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepositoryQuery {

    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable, String weather, LocalDateTime startDate, LocalDateTime endDate);

    Page<TodoQueryResponse> findAllByOrderByCreatedAtDesc(Pageable pageable, String search, LocalDateTime startDate, LocalDateTime endDate);

    Optional<Todo> findByIdWithUser(Long todoId);
}
