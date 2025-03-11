package org.example.expert.domain.common.dto;

import lombok.Getter;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthUser implements UserDetails {

    private final Long id;
    private final String email;
    private final UserRole userRole;
    private final String nickname;

    public AuthUser(Long id, String email, UserRole userRole, String nickname) {
        this.id = id;
        this.email = email;
        this.userRole = userRole;
        this.nickname = nickname;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userRole.name()));
    }

    @Override
    public String getPassword() {
        return null;        // JWT 기반 인증에서는 사용되지 않음
    }

    @Override
    public String getUsername() {
        // 어떻게 해야하나 고민했는데 단순히 Email을 대체로 사용하면 된다고함
        return email;
    }

    // 과제 요구사항에 없어서 아래 내용들은 true 로 설정하여 항상 로그인을 가능하게 만들었음
    @Override               // 계정이 만료되지 않았는지
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override               // 계정이 잠금되지 않았는지
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override               // 비밀번호가 만료되지 않았는지
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override               // 계정이 활성화 되어 있는지
    public boolean isEnabled() {
        return true;
    }

}
