package org.example.expert.domain.todo.service

import org.example.expert.client.WeatherClient
import org.example.expert.common.exception.InvalidRequestException
import org.example.expert.domain.common.dto.AuthUser
import org.example.expert.domain.todo.dto.request.TodoSaveRequest
import org.example.expert.domain.todo.dto.response.TodoQueryResponse
import org.example.expert.domain.todo.dto.response.TodoResponse
import org.example.expert.domain.todo.dto.response.TodoSaveResponse
import org.example.expert.domain.todo.entity.Todo
import org.example.expert.domain.todo.repository.TodoRepository
import org.example.expert.domain.user.dto.response.UserResponse
import org.example.expert.domain.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.math.max

@Service
@Transactional(readOnly = true)
class TodoService(
    private val todoRepository: TodoRepository,
    private val weatherClient: WeatherClient
) {

    @Transactional
    fun saveTodo(authUser: AuthUser, todoSaveRequest: TodoSaveRequest): TodoSaveResponse {
        val user: User = User.fromAuthUser(authUser)

        val weather: String = weatherClient.todayWeather

        val newTodo = Todo.create(
            todoSaveRequest.title,
            todoSaveRequest.contents,
            weather,
            user
        )

        val savedTodo = todoRepository.save(newTodo)

        return TodoSaveResponse(
            savedTodo.id,
            savedTodo.title,
            savedTodo.contents,
            weather,
            UserResponse(user.id, user.email)
        )
    }

    fun getTodosQuery(page: Int, size: Int, search: String?, startDate: LocalDateTime?, endDate: LocalDateTime?)
            : Page<TodoQueryResponse> {
        val pageable = PageRequest.of(max(0, page - 1), size)
        return todoRepository.findAllByOrderByCreatedAtDesc(pageable, search, startDate, endDate)
    }

    fun getTodos(page: Int, size: Int, weather: String?, startDate: LocalDateTime?, endDate: LocalDateTime?):
            Page<TodoResponse> {
        val pageable = PageRequest.of(page - 1, size)

        val todos: Page<Todo> =
            todoRepository.findAllByOrderByModifiedAtDesc(pageable, weather, startDate, endDate)

        return todos.map {
            TodoResponse(
                it.id,
                it.title,
                it.contents,
                it.weather,
                UserResponse(it.user.id, it.user.email),
                it.createdAt,
                it.modifiedAt
            )
        }
    }

    fun getTodo(todoId: Long): TodoResponse {
        val findTodo: Todo = todoRepository.findByIdWithUser(todoId)
            ?: throw InvalidRequestException("Todo not found")

        val user = findTodo.user

        return TodoResponse(
            findTodo.id,
            findTodo.title,
            findTodo.contents,
            findTodo.weather,
            UserResponse(user.id, user.email),
            findTodo.createdAt,
            findTodo.modifiedAt
        )
    }
}