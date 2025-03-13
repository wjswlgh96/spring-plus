package org.example.expert.domain.common.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(errorOnInvalidType = true)     // AuthUser 타입이 아닌 경우 예외 발생 시킴
public @interface Auth {
}
