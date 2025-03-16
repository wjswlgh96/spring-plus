package org.example.expert.domain.user.dto.response;

import lombok.Getter;

import java.util.UUID;

@Getter
public class UserResponse {

    private final UUID id;
    private final String email;

    public UserResponse(UUID id, String email) {
        this.id = id;
        this.email = email;
    }
}
