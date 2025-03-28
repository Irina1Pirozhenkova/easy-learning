package com.example.easy_learning.service;

import com.example.easy_learning.model.Homework;
import com.example.easy_learning.model.HomeworkTask;
import com.example.easy_learning.model.Task;
import com.example.easy_learning.repository.HomeworkRepository;
import com.example.easy_learning.repository.TaskRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class HomeworkService {

  private final HomeworkRepository homeworkRepository;
  private final TaskRepository taskRepository;
  private final EntityManager entityManager;

  public Homework createHomework(Homework homework) {
    return homeworkRepository.save(homework);
  }

  public Homework getHomeworkById(Integer id) {
    return homeworkRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Homework not found with id: " + id));
  }

  public Homework getHomeworkWithAssociationsById(Integer id) {
    return homeworkRepository.findHomeworkWithAssociationsById(id)
            .orElseThrow(() -> new RuntimeException("Homework not found with id: " + id));
  }

  public Homework updateHomework(Integer id, Homework updatedHomework) {
    Homework existing = getHomeworkWithAssociationsById(id);

    System.out.println(entityManager.contains(existing));

    if (updatedHomework.getTasks() != null) existing.setTasks(updatedHomework.getTasks());
    if (updatedHomework.getStudents() != null) existing.setStudents(updatedHomework.getStudents());
    if (updatedHomework.getTutor() != null) existing.setTutor(updatedHomework.getTutor());

    existing.setClassName(updatedHomework.getClassName());
    existing.setSubject(updatedHomework.getSubject());
    existing.setTopic(updatedHomework.getTopic());
    existing.setDifficulty(updatedHomework.getDifficulty());

    existing = homeworkRepository.save(existing);
    return existing;
  }

  public void deleteHomework(Integer id) {
    homeworkRepository.deleteById(id);
  }

  public List<Homework> getAllHomeworks() {
    return homeworkRepository.findAll();
  }

  /**
   * Добавляет новые задачи (HomeworkTask) в домашнее задание.
   * Если хотя бы один из переданных taskId уже присутствует в Homework,
   * метод бросает исключение.
   *
   * @param homeworkId идентификатор домашнего задания
   * @param taskIds набор id задач для добавления
   * @return обновлённое домашнее задание с добавленными связями
   */
  @Transactional
  public Homework addTasksToHomework(Integer homeworkId, Set<Integer> taskIds) {
    Homework homework = getHomeworkWithAssociationsById(homeworkId);

    // Проверяем пересечение: если для какого-либо taskId уже существует связь, бросаем исключение
    for (Integer taskId : taskIds) {
      boolean exists = homework.getTasks().stream()
              .anyMatch(ht -> ht.getTask().getId().equals(taskId));
      if (exists) {
        throw new RuntimeException("Task with id " + taskId + " already exists in Homework");
      }
    }

    // Для каждого нового id создаём связь HomeworkTask
    for (Integer taskId : taskIds) {
      Task task = taskRepository.findById(taskId)
              .orElseThrow(() -> new RuntimeException("Task with id " + taskId + " not found"));
      HomeworkTask ht = new HomeworkTask();
      // Используем сеттеры, которые автоматически добавляют связь в коллекции
      ht.setTask(task);
      ht.setHomework(homework);
    }

    return homeworkRepository.save(homework);
  }

  /**
   * Удаляет из домашнего задания связи с задачами.
   * Для каждого объекта HomeworkTask, у которого task имеет id, входящий в taskIds,
   * вызывается setHomework(null) (для orphanRemoval) и объект удаляется из коллекции.
   *
   * @param homeworkId идентификатор домашнего задания
   * @param taskIds список id задач, связи с которыми необходимо удалить
   * @return обновлённое домашнее задание без указанных связей
   */
  @Transactional
  public Homework removeTasksFromHomework(Integer homeworkId, List<Integer> taskIds) {
    Homework homework = getHomeworkWithAssociationsById(homeworkId);

    homework.getTasks().removeIf(ht -> {
      if (taskIds.contains(ht.getTask().getId())) {
        ht.setHomework(null);
        return true;
      }
      return false;
    });

    return homeworkRepository.save(homework);
  }
}
