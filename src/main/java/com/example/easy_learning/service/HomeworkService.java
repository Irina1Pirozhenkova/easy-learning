package com.example.easy_learning.service;

import com.example.easy_learning.model.Homework;

import java.util.List;
import java.util.Set;

public interface HomeworkService {
  Homework createHomework(Homework homework);
  Homework getHomeworkById(Integer id);
  Homework getHomeworkWithAssociationsById(Integer id);
  Homework updateHomework(Integer id, Homework updatedHomework);
  void deleteHomework(Integer id);
  List<Homework> getAllHomeworks();
  Homework addTasksToHomework(Integer homeworkId, Set<Integer> taskIds);
  Homework removeTasksFromHomework(Integer homeworkId, List<Integer> taskIds);
}
