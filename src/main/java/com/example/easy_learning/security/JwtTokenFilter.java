package com.example.easy_learning.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtTokenFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @SneakyThrows
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        String bearerToken = ((HttpServletRequest) servletRequest).getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {//Получаем заголовок Authorization
            bearerToken = bearerToken.substring(7);
        }

        try {//Получаем объект Authentication, в котором содержится пользователь,
            if (bearerToken != null && jwtTokenProvider.isValid(bearerToken)) { //Проверяем: токен есть и он валидный
                Authentication authentication = jwtTokenProvider.getAuthentication(bearerToken);
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }//Вставляем Authentication в контекст безопасности, чтобы Spring знал: "этот пользователь авторизован"
            }
        } catch (Exception ignored) {
        }

        filterChain.doFilter(servletRequest, servletResponse);
        //Передаём управление дальше — другим фильтрам, контроллерам и т.д.
    }
}
