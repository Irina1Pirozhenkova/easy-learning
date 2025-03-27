package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.StudentNRDto;
import com.example.easy_learning.dto.StudentRDto;
import com.example.easy_learning.model.Student;
import com.example.easy_learning.model.StudentPersonalInfo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {StudentPersonalInfoMapper.class})
public interface StudentMapper {

  Student toNREntity(StudentNRDto studentNRDto);

  StudentNRDto toNRDto(Student student);

  StudentRDto toRDto(StudentNRDto studentNRDto);
  StudentNRDto toNRDto(StudentRDto studentRDto);

  List<Student> toNREntities(List<StudentNRDto> studentNRDtos);

  List<StudentNRDto> toNRDtos(List<Student> students);
  List<StudentRDto> toRDtos(List<StudentNRDto> studentNRDtoss);
}
