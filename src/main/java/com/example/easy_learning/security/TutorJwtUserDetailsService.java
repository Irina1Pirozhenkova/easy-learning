package com.example.easy_learning.security;

import com.example.easy_learning.model.Tutor;
import com.example.easy_learning.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TutorJwtUserDetailsService implements UserDetailsService {

    private final TutorService tutorService;

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        Tutor tutor = tutorService.getByEmail(email);
        return TutorJwtEntityFactory.create(tutor);
    }
}
