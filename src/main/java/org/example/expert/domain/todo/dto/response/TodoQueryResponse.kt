package org.example.expert.domain.todo.dto.response

import java.time.LocalDateTime

data class TodoQueryResponse(
    val title: String,
    val managerCount: Int,
    val commentCount: Int,
    val createdAt: LocalDateTime
) {}