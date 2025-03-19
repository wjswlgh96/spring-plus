package org.example.expert.domain.todo.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.expert.common.config.SecurityConfig
import org.example.expert.common.exception.InvalidRequestException
import org.example.expert.common.secutiry.JwtAuthenticationToken
import org.example.expert.domain.common.dto.AuthUser
import org.example.expert.domain.todo.dto.request.TodoSaveRequest
import org.example.expert.domain.todo.service.TodoService
import org.example.expert.domain.user.enums.UserRole
import org.example.expert.utils.JwtUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.doThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@WebMvcTest(TodoController::class)
@Import(SecurityConfig::class, JwtUtil::class)
class TodoControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var todoService: TodoService

    private lateinit var token: JwtAuthenticationToken

    @BeforeEach
    fun setUp() {
        val authUser = AuthUser(UUID.randomUUID(), "abc@abc.com", UserRole.ROLE_ADMIN, "테스트 유저")
        token = JwtAuthenticationToken(authUser)
    }

    @DisplayName("투두 생성하기 - 성공")
    @Test
    fun saveTodo1() {
        // given
        val request = TodoSaveRequest(
            "제목",
            "내용"
        )

        // when & then
        mockMvc.perform(
            post("/todos")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
                .with(authentication(token))
        )
            .andExpect(status().isOk())
    }

    @DisplayName("투두 생성하기 - 권한 없음(403 - FORBIDDEN)")
    @Test
    fun saveTodo2() {
        // given
        val request = TodoSaveRequest(
            "제목",
            "내용"
        )

        // when & then
        mockMvc.perform(
            post("/todos")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request))
        )
            .andExpect(status().isForbidden)
    }

    @DisplayName("투두 쿼리 목록 조회 - 성공")
    @Test
    fun getTodosQuery1() {
        // when & then
        mockMvc.perform(
            get("/todos-query")
                .contentType(APPLICATION_JSON)
                .with(authentication(token))
        )
            .andExpect(status().isOk())
    }

    @DisplayName("투두 쿼리 목록 조회 - 권한 없음(403 - FORBIDDEN)")
    @Test
    fun getTodosQuery2() {
        // when & then
        mockMvc.perform(
            get("/todos-query")
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isForbidden())
    }

    @DisplayName("투두 목록 조회 - 성공")
    @Test
    fun getTodos1() {
        // when & then
        mockMvc.perform(
            get("/todos")
                .contentType(APPLICATION_JSON)
                .with(authentication(token))

        )
            .andExpect(status().isOk())
    }

    @DisplayName("투두 목록 조회 - 권한 없음(403 - FORBIDDEN)")
    @Test
    fun getTodos2() {
        // when & then
        mockMvc.perform(
            get("/todos")
                .contentType(APPLICATION_JSON)

        )
            .andExpect(status().isForbidden)
    }

    @DisplayName("투두 단건 조회 - 성공")
    @Test
    fun getTodo1() {
        // given
        val todoId = 1L

        // when & then
        mockMvc.perform(
            get("/todos/{todoId}", todoId)
                .contentType(APPLICATION_JSON)
                .with(authentication(token))
        )
            .andExpect(status().isOk)
    }

    @DisplayName("투두 단건 조회 - 존재하지 않는 투두(400 - BAD_REQUEST)")
    @Test
    fun getTodo2() {
        // given
        val todoId = 1L

        // when
        doThrow(InvalidRequestException("Todo not found"))
            .`when`(todoService).getTodo(anyLong())

        // when & then
        mockMvc.perform(
            get("/todos/{todoId}", todoId)
                .contentType(APPLICATION_JSON)
                .with(authentication(token))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Todo not found"))
    }

    @DisplayName("투두 단건 조회 - 권한 없음(403 - FORBIDDEN)")
    @Test
    fun getTodo3() {
        // given
        val todoId = 1L

        // when & then
        mockMvc.perform(
            get("/todos/{todoId}", todoId)
                .contentType(APPLICATION_JSON)
        )
            .andExpect(status().isForbidden)
    }

}