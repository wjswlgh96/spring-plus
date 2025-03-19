package org.example.expert.domain.todo.controller

import jakarta.validation.Valid
import org.example.expert.domain.common.annotation.Auth
import org.example.expert.domain.common.dto.AuthUser
import org.example.expert.domain.todo.dto.request.TodoSaveRequest
import org.example.expert.domain.todo.dto.response.TodoQueryResponse
import org.example.expert.domain.todo.dto.response.TodoResponse
import org.example.expert.domain.todo.dto.response.TodoSaveResponse
import org.example.expert.domain.todo.service.TodoService
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
class TodoController(
    private val todoService: TodoService
) {

    @PostMapping("/todos")
    fun saveTodo(
        @Auth authUser: AuthUser,
        @Valid @RequestBody todoSaveRequest: TodoSaveRequest
    ): ResponseEntity<TodoSaveResponse> {
        return ResponseEntity.ok(todoService.saveTodo(authUser, todoSaveRequest))
    }

    @GetMapping("/todos-query")
    fun getTodosQuery(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(name = "search", required = false) search: String?,
        @RequestParam(name = "startDate", required = false) startDate: LocalDateTime?,
        @RequestParam(name = "endDate", required = false) endDateTime: LocalDateTime?
    ): ResponseEntity<Page<TodoQueryResponse>> {
        return ResponseEntity.ok(todoService.getTodosQuery(page, size, search, startDate, endDateTime))
    }

    @GetMapping("/todos")
    fun getTodos(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(name = "weather", required = false) weather: String?,
        @RequestParam(name = "startDate", required = false) startDate: LocalDateTime?,
        @RequestParam(name = "endDate", required = false) endDate: LocalDateTime?
    ): ResponseEntity<Page<TodoResponse>> {
        return ResponseEntity.ok(todoService.getTodos(page, size, weather, startDate, endDate))
    }

    @GetMapping("/todos/{todoId}")
    fun getTodo(@PathVariable todoId: Long): ResponseEntity<TodoResponse> {
        return ResponseEntity.ok(todoService.getTodo(todoId))
    }
}