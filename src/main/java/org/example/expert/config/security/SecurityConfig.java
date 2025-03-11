package org.example.expert.config.security;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, AuthenticationManager authenticationManager) throws Exception {

        JwtAuthorizationFilter jwtAuthorizationFilter =
            new JwtAuthorizationFilter(authenticationManager, jwtUtil);

        return httpSecurity
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(
                auth ->
                    auth
                        .requestMatchers("/auth/**", "/h2-console/**").permitAll()          // 인증 없이 허용
                        .requestMatchers("/admin/**").hasAuthority(UserRole.ADMIN.name())     // /admin 하위 path는 UserRole이 ADMIN 이어야 허용
                        .anyRequest().authenticated()                                           // 나머지는 인증 필요함
            )
            .exceptionHandling(ex -> ex.accessDeniedHandler(new AccessDeniedHandler()))
            .build();
    }

    // CORS 설정, 설정을 안해주면 다른 도메인에서 요청시 CORS 에러가 발생함
    // 다른 도메인이란, 예를 들어 프론트엔드가 요청하는 도메인을 말한다.
    // 백엔드, 프론트엔드가 같은 도메인에서 동작하는 경우 CORS 설정이 필요없다.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);                                                   // 쿠키 포함 요청을 허용
        config.setAllowedOrigins(List.of("*"));                                         // 모든 도메인 요청 허용
        config.setAllowedHeaders(List.of("*"));                                         // 모든 헤더 요청 허용
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));         // 허용할 HTTP 메서드
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);                             // 모든 경로(API 엔드포인트)에 CORS 적용
        return source;
    }
}
