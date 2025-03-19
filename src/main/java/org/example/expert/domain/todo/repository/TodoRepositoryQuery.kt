package org.example.expert.domain.todo.repository

import org.example.expert.domain.todo.dto.response.TodoQueryResponse
import org.example.expert.domain.todo.entity.Todo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.util.*

interface TodoRepositoryQuery {

    fun findAllByOrderByModifiedAtDesc(
        pageable: Pageable,
        weather: String?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?
    ): Page<Todo>

    fun findAllByOrderByCreatedAtDesc(
        pageable: Pageable,
        search: String?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?
    ): Page<TodoQueryResponse>

    fun findByIdWithUser(
        todoId: Long
    ): Todo?

}