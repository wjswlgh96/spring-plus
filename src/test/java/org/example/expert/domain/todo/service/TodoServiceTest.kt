package org.example.expert.domain.todo.service

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.example.expert.client.WeatherClient
import org.example.expert.common.exception.InvalidRequestException
import org.example.expert.domain.common.dto.AuthUser
import org.example.expert.domain.todo.dto.request.TodoSaveRequest
import org.example.expert.domain.todo.dto.response.TodoResponse
import org.example.expert.domain.todo.entity.Todo
import org.example.expert.domain.todo.repository.TodoRepository
import org.example.expert.domain.user.entity.User
import org.example.expert.domain.user.enums.UserRole
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class TodoServiceTest {

    @Autowired
    private lateinit var todoService: TodoService

    @Autowired
    private lateinit var todoRepository: TodoRepository

    @Autowired
    private lateinit var weatherClient: WeatherClient

    @PersistenceContext
    private lateinit var em: EntityManager

    private lateinit var authUser: AuthUser

    @BeforeEach
    fun setUp() {
        val user = User(
            "abc@abc.com",
            "Password1234!",
            UserRole.ROLE_ADMIN,
            "테스트유저"
        )

        em.persist(user)
        em.flush()
        authUser = AuthUser(
            user.id,
            user.email,
            user.userRole,
            user.nickname
        )
        em.clear()
    }

    @DisplayName("투두가 성공적으로 생성된다.")
    @Test
    fun saveTodo() {
        // given
        val request = TodoSaveRequest(
            "제목",
            "내용"
        )

        val weather = weatherClient.todayWeather

        // when
        val response = todoService.saveTodo(authUser, request)

        // then
        assertThat(response)
            .extracting("id", "title", "contents", "weather")
            .containsExactly(1L, request.title, request.contents, weather)

        assertThat(response.user)
            .extracting("id", "email")
            .containsExactly(authUser.id, authUser.email)
    }

    @DisplayName("투두 단건 조회 성공")
    @Test
    fun getTodo1() {
        // given
        val user = User.fromAuthUser(authUser)
        val weather = weatherClient.todayWeather

        val savedTodo = todoRepository.save(
            createTestTodo(
                "제목", "내용", weather, user
            )
        )

        // when
        val response: TodoResponse = todoService.getTodo(savedTodo.id!!)

        // then
        assertThat(response)
            .extracting("id", "title", "contents", "weather")
            .containsExactly(savedTodo.id, "제목", "내용", weather)

        assertThat(response.user)
            .extracting("id", "email")
            .containsExactly(user.id, user.email)
    }

    @DisplayName("존재하지 않는 투두 조회시 예외가 발생한다.")
    @Test
    fun getTodo2() {
        // given
        val todoId = -1L

        // when & then
        assertThatThrownBy {
            todoService.getTodo(todoId)
        }
            .isInstanceOf(InvalidRequestException::class.java)
            .hasMessage("Todo not found")
    }

    private fun createTestTodo(
        title: String,
        contents: String,
        weather: String,
        user: User
    ): Todo {
        return Todo.create(
            title,
            contents,
            weather,
            user
        )
    }
}