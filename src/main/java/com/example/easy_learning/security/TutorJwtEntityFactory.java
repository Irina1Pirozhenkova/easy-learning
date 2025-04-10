package com.example.easy_learning.security;

import com.example.easy_learning.model.Tutor;

import java.util.Collections;

public final class TutorJwtEntityFactory {

    public static TutorJwtEntity create(Tutor tutor) {
        return new TutorJwtEntity(
                tutor.getId(),
                tutor.getPersonalInfo().getEmail(),
                tutor.getPassword()
        );
    }
}
