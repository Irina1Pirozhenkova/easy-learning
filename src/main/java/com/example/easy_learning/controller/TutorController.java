package com.example.easy_learning.controller;

import com.example.easy_learning.dto.TutorNRDto;
import com.example.easy_learning.dto.TutorRDto;
import com.example.easy_learning.mapper.HomeworkMapper;
import com.example.easy_learning.mapper.TaskMapper;
import com.example.easy_learning.mapper.TutorMapper;
import com.example.easy_learning.model.Tutor;
import com.example.easy_learning.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tutor")
@RequiredArgsConstructor
public class TutorController {

  private final TutorService tutorService;
  private final TutorMapper tutorMapper;
  private final HomeworkMapper homeworkMapper;
  private final TaskMapper taskMapper;

  @PostMapping
  public ResponseEntity<TutorNRDto> createTutor(@RequestBody TutorNRDto tutorNRDto) {
    Tutor toCreate = tutorMapper.toNREntity(tutorNRDto);
    TutorNRDto created = tutorMapper.toNRDto(tutorService.create(toCreate));
    return ResponseEntity.ok(created);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getTutor(@PathVariable Integer id,
                                      @RequestParam(required = false, defaultValue = "false") boolean full) {
    if (full) {
      Tutor tutor = tutorService.getById(id);
      TutorRDto tutorRDto = tutorMapper.toRDto(tutorMapper.toNRDto(tutor));
      tutorRDto.setHomeworks(homeworkMapper.toNRDtos(tutor.getHomeworks()));
      tutorRDto.setTasks(taskMapper.toNRDtos(tutor.getTasks()));
      tutorRDto.setPassword(null);
      return ResponseEntity.ok(tutorRDto);
    }
    TutorNRDto tutorNRDto = tutorMapper.toNRDto(tutorService.getById(id));
    tutorNRDto.setPassword(null);
    return ResponseEntity.ok(tutorNRDto);
  }

  @GetMapping
  public ResponseEntity<Set<TutorNRDto>> getAllTutors() {
    Set<Tutor> tutors = tutorService.getAll();
    Set<TutorNRDto> tutorNRDtos = tutorMapper.toNRDtos(Set.copyOf(tutors))
            .stream().map(s -> {
              s.setPassword(null);
              return s;
            }).collect(Collectors.toSet());
    return ResponseEntity.ok(tutorNRDtos);
  }

  @PutMapping("/{id}")
  public ResponseEntity<TutorRDto> updateTutor(@PathVariable Integer id, @RequestBody TutorRDto tutorRDto) {
    Tutor toUpdate = tutorMapper.toNREntity(tutorMapper.toNRDto(tutorRDto));
    if (tutorRDto.getHomeworks() != null)
      toUpdate.setHomeworks(Set.copyOf(homeworkMapper.toNREntities(List.copyOf(tutorRDto.getHomeworks()))));

    Tutor updated = tutorService.update(id, toUpdate);

    tutorRDto = tutorMapper.toRDto(tutorMapper.toNRDto(updated));
    tutorRDto.setHomeworks(homeworkMapper.toNRDtos(updated.getHomeworks()));
    return ResponseEntity.ok(tutorRDto);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTutor(@PathVariable Integer id) {
    return ResponseEntity.noContent().build();
  }
}
