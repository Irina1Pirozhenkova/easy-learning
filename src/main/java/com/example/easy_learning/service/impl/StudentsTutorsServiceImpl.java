package com.example.easy_learning.service.impl;

import com.example.easy_learning.model.StudentsTasks;
import com.example.easy_learning.model.StudentsTutors;
import com.example.easy_learning.model.Task;
import com.example.easy_learning.model.User;
import com.example.easy_learning.repository.StudentsTasksRepository;
import com.example.easy_learning.repository.StudentsTutorsRepository;
import com.example.easy_learning.repository.TaskRepository;
import com.example.easy_learning.repository.UserRepository;
import com.example.easy_learning.service.StudentsTutorsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentsTutorsServiceImpl implements StudentsTutorsService {

  private final StudentsTutorsRepository linkRepo;
  private final UserRepository userRepo;
  private final TaskRepository taskRepo;
  private final StudentsTasksRepository studentsTasksRepo;

  @Override
  public List<User> getStudentsForTutor(Integer tutorId) {
    User tutor = userRepo.findById(tutorId)
            .orElseThrow(() -> new RuntimeException("Tutor not found: " + tutorId));
    return tutor.getStudents().stream()
            .map(StudentsTutors::getStudent)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void addStudentToTutor(Integer tutorId, Integer studentId) {
    User tutor = userRepo.findById(tutorId).orElseThrow(() -> new RuntimeException("Tutor not found"));
    User student = userRepo.findById(studentId).orElseThrow(() -> new RuntimeException("Student not found"));

    // Проверка: уже существует такая связь?
    boolean exists = linkRepo.existsByTutorAndStudent(tutor, student);
    if (exists) {
      throw new IllegalStateException("Студент уже привязан к этому репетитору");
    }

    StudentsTutors link = new StudentsTutors();
    link.setTutor(tutor);
    link.setStudent(student);
    linkRepo.save(link);
  }


  @Override
  public List<User> getTutorsForStudent(Integer studentId) {
    User student = userRepo.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Студент не найден: " + studentId));
    return student.getTutors().stream()
            .map(StudentsTutors::getTutor)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void assignTaskToStudent(Integer tutorId, Integer taskId, Integer studentId) {
    // проверяем, что этот студент действительно привязан к этому тьютору
    User tutor = userRepo.findById(tutorId).orElseThrow();
    User student = userRepo.findById(studentId).orElseThrow();
    boolean linked = tutor.getStudents().stream()
            .map(StudentsTutors::getStudent)
            .anyMatch(s -> s.getId().equals(studentId));
    if (!linked) {
      throw new RuntimeException("Студент не привязан к репетитору");
    }

    Task task = taskRepo.findById(taskId).orElseThrow();
    // создаём запись StudentsTasks
    StudentsTasks st = new StudentsTasks();
    st.setStudent(student);
    st.setTask(task);
    st.setIsDone(false);
    st.setIsChecked(false);
    st.setScore(null);
    studentsTasksRepo.save(st);
  }
}
