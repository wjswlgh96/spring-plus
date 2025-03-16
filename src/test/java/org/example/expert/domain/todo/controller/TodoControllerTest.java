package org.example.expert.domain.todo.controller;

import org.example.expert.utils.JwtUtil;
import org.example.expert.common.secutiry.JwtAuthenticationToken;
import org.example.expert.common.config.SecurityConfig;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodoController.class)
@Import({SecurityConfig.class, JwtUtil.class})
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    @Test
    void todo_단건_조회에_성공한다() throws Exception {
        // given
        long todoId = 1L;
        String title = "title";
        AuthUser authUser = new AuthUser(UUID.randomUUID(), "email", UserRole.ROLE_USER, "user");

        JwtAuthenticationToken token = new JwtAuthenticationToken(authUser);

        User user = User.fromAuthUser(authUser);
        UserResponse userResponse = new UserResponse(user.getId(), user.getEmail());
        TodoResponse response = new TodoResponse(
            todoId,
            title,
            "contents",
            "Sunny",
            userResponse,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        // when
        when(todoService.getTodo(todoId)).thenReturn(response);

        // then
        mockMvc.perform(get("/todos/{todoId}", todoId)
                .with(authentication(token))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(todoId))
            .andExpect(jsonPath("$.title").value(title));
    }

    @Test
    void todo_단건_조회_시_todo가_존재하지_않아_예외가_발생한다() throws Exception {
        // given
        long todoId = 1L;
        AuthUser authUser = new AuthUser(UUID.randomUUID(), "email", UserRole.ROLE_USER, "user");

        JwtAuthenticationToken token = new JwtAuthenticationToken(authUser);

        // when
        when(todoService.getTodo(todoId))
            .thenThrow(new InvalidRequestException("Todo not found"));

        // then
        mockMvc.perform(get("/todos/{todoId}", todoId)
                .with(authentication(token))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.message").value("Todo not found"));
    }
}
