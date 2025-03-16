package org.example.expert.domain.common.dto;

import lombok.Getter;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class AuthUser {

    private final UUID id;
    private final String email;
    private final UserRole userRole;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String nickname;

    public AuthUser(UUID id, String email, UserRole userRole, String nickname) {
        this.id = id;
        this.email = email;
        this.userRole = userRole;
        this.authorities = List.of(new SimpleGrantedAuthority(userRole.name()));
        this.nickname = nickname;
    }

}
