package com.example.easy_learning.service;

import com.example.easy_learning.model.Tutor;

import java.util.List;
import java.util.Set;

public interface TutorService {

  Tutor create(Tutor tutor);

  Tutor update(Integer id, Tutor updatedTutor);

  Tutor getById(Integer id);

  Tutor getByEmail(String email);


  Tutor getByIdWithRelations(Integer id);

  Set<Tutor> getAll();
}
