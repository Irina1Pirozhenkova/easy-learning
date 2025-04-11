package com.example.easy_learning.service.impl;

import com.example.easy_learning.model.Homework;
import com.example.easy_learning.model.HomeworkTask;
import com.example.easy_learning.model.Task;
import com.example.easy_learning.repository.HomeworkRepository;
import com.example.easy_learning.repository.TaskRepository;
import com.example.easy_learning.service.HomeworkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HomeworkServiceImpl implements HomeworkService {

    private final HomeworkRepository homeworkRepository;
    private final TaskRepository taskRepository;


    @Override
    public Homework createHomework(Homework homework) {
        return homeworkRepository.save(homework);
    }

    @Override
    public Homework getHomeworkWithAssociationsById(Integer id) {
        return homeworkRepository.findHomeworkWithAssociationsById(id)
                .orElseThrow(() -> new RuntimeException("Homework not found"));
    }

    @Override
    public List<Homework> getAllHomeworks() {
        return homeworkRepository.findAll();
    }

    @Override
    public Homework updateHomework(Integer id, Homework updatedHomework) {
        Homework existing = getHomeworkWithAssociationsById(id);
        existing.setClassName(updatedHomework.getClassName());
        existing.setSubject(updatedHomework.getSubject());
        existing.setTopic(updatedHomework.getTopic());
        existing.setDifficulty(updatedHomework.getDifficulty());
        existing.setTutor(updatedHomework.getTutor());
        existing.setStudents(updatedHomework.getStudents());
        existing.setTasks(updatedHomework.getTasks());
        return homeworkRepository.save(existing);
    }

    @Override
    public void deleteHomework(Integer id) {
        homeworkRepository.deleteById(id);
    }

    @Override
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
    @Override
    public Homework getHomeworkById(Integer id) {
        return homeworkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Homework not found with id: " + id));
    }
    @Override
    public Homework addTasksToHomework(Integer homeworkId, Set<Integer> taskIds) {
        Homework homework = homeworkRepository.findHomeworkWithAssociationsById(homeworkId)
                .orElseThrow(() -> new RuntimeException("Homework not found with id: " + homeworkId));

        for (Integer taskId : taskIds) {
            boolean exists = homework.getTasks().stream()
                    .anyMatch(ht -> ht.getTask().getId().equals(taskId));
            if (exists) {
                throw new RuntimeException("Task with id " + taskId + " already exists in Homework");
            }

            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task with id " + taskId + " not found"));
            HomeworkTask ht = new HomeworkTask();
            ht.setTask(task);
            ht.setHomework(homework);
        }

        return homeworkRepository.save(homework);
    }

}
