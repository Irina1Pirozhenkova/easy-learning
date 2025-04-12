package com.example.easy_learning.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import java.util.Collections;

@Getter
@AllArgsConstructor
public class StudentJwtEntity implements UserDetails {

    private final Integer id;
    private final String username; // email
    private final String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // если ролей нет
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }//У пользователя не просрочен аккаунт

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }//Не заблокирован

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }//Пароль не истёк

    @Override
    public boolean isEnabled() {
        return true;
    }//И он активен
}
