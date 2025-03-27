package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.StudentNRDto;
import com.example.easy_learning.dto.StudentRDto;
import com.example.easy_learning.model.Student;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface StudentMapper {

  Student toNREntity(StudentNRDto studentNRDto);

  StudentNRDto toNRDto(Student student);
  StudentRDto toRDto(StudentNRDto studentNRDto);

  Set<Student> toNREntities(Set<StudentNRDto> studentNRDtos);

  Set<StudentNRDto> toNRDtos(Set<Student> students);
  Set<StudentRDto> toRDtos(Set<StudentNRDto> studentNRDtoss);
}
