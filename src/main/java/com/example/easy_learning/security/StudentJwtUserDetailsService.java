package com.example.easy_learning.security;

import com.example.easy_learning.model.Student;
import com.example.easy_learning.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentJwtUserDetailsService implements UserDetailsService {

    private final StudentService studentService;
    //ищет пользователя по email через StudentService
    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        Student student = studentService.getByEmail(email);
        return StudentJwtEntityFactory.create(student);
    }
}
