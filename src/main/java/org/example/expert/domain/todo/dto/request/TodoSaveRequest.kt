package org.example.expert.domain.todo.dto.request

import jakarta.validation.constraints.NotBlank

data class TodoSaveRequest(
    @NotBlank
    var title: String,

    @NotBlank
    var contents: String
) {}