package org.example.expert.domain.todo.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class TodoQueryResponse {

    private final String title;
    private final int managerCount;
    private final int commentCount;
    private final LocalDateTime createdAt;
}
