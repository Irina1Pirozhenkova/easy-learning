package com.example.easy_learning.service;

import com.example.easy_learning.model.User;

import java.util.List;

public interface StudentsTutorsService {
  List<User> getStudentsForTutor(Integer tutorId);

  void addStudentToTutor(Integer tutorId, Integer studentId);

  List<User> getTutorsForStudent(Integer studentId);

  void assignTaskToStudent(Integer tutorId, Integer taskId, Integer studentId);
}
