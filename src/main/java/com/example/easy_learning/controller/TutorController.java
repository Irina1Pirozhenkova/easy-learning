package com.example.easy_learning.controller;

import com.example.easy_learning.dto.TutorNRDto;
import com.example.easy_learning.dto.TutorRDto;
import com.example.easy_learning.mapper.*;
import com.example.easy_learning.model.Tutor;
import com.example.easy_learning.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tutors")
@RequiredArgsConstructor
public class TutorController {

    private final TutorService tutorService;
    private final TutorMapper tutorMapper;
    private final HomeworkMapper homeworkMapper;
    private final TaskMapper taskMapper;
    private final StudentsTutorsMapper studentsTutorsMapper;

    @PostMapping
    public ResponseEntity<TutorNRDto> create(@RequestBody TutorNRDto tutorNRDto) {
        Tutor tutor = tutorMapper.toNREntity(tutorNRDto);
        Tutor saved = tutorService.create(tutor);
        return ResponseEntity.ok(tutorMapper.toNRDto(saved));
    }
/*
    @PutMapping("/{id}")
    public ResponseEntity<TutorRDto> update(@PathVariable Integer id, @RequestBody TutorRDto tutorRDto) {
        Tutor updatedTutor = tutorMapper.toEntity(tutorRDto);

        if (tutorRDto.getTasks() != null) {
            updatedTutor.setTasks(taskMapper.toNREntities(tutorRDto.getTasks()));
        }
        if (tutorRDto.getHomeworks() != null) {
            updatedTutor.setHomeworks(homeworkMapper.toNREntities(tutorRDto.getHomeworks()));
        }
        if (tutorRDto.getStudents() != null) {
            updatedTutor.setStudents(studentsTutorsMapper.toEntitiesFromTDto(tutorRDto.getStudents()));
        }

        Tutor updated = tutorService.update(id, updatedTutor);
        TutorRDto responseDto = tutorMapper.toRDto(updated);


        TutorRDto responseDto = tutorMapper.toRDto(tutorMapper.toNRDto(updated));
        responseDto.setTasks(taskMapper.toNRDtos(updated.getTasks()));
        responseDto.setHomeworks(homeworkMapper.toNRDtos(updated.getHomeworks()));
        responseDto.setStudents(studentsTutorsMapper.toTDtoSet(updated.getStudents()));
        responseDto.setPassword(null);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id,
                                     @RequestParam(defaultValue = "false") boolean full) {
        Tutor tutor = full ? tutorService.getByIdWithRelations(id) : tutorService.getById(id);
        if (full) {
            TutorRDto tutorRDto = tutorMapper.toRDto(tutorMapper.toNRDto(tutor));
            tutorRDto.setTasks(taskMapper.toNRDtos(tutor.getTasks()));
            tutorRDto.setHomeworks(homeworkMapper.toNRDtos(tutor.getHomeworks()));
            tutorRDto.setStudents(studentsTutorsMapper.toTDtoSet(tutor.getStudents()));
            tutorRDto.setPassword(null);
            return ResponseEntity.ok(tutorRDto);
        } else {
            TutorNRDto tutorNRDto = tutorMapper.toNRDto(tutor);
            tutorNRDto.setPassword(null);
            return ResponseEntity.ok(tutorNRDto);
        }
    }

    @GetMapping
    public ResponseEntity<List<TutorNRDto>> getAll() {
        List<TutorNRDto> dtos = tutorService.getAll().stream()
                .map(tutorMapper::toNRDto)
                .peek(t -> t.setPassword(null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }*/
}
