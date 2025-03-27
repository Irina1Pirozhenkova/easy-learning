package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.StudentsTutorsSDto;
import com.example.easy_learning.dto.StudentsTutorsTDto;
import com.example.easy_learning.model.StudentsTutors;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {StudentMapper.class, TutorMapper.class})
public interface StudentsTutorsMapper {
  StudentsTutorsSDto toSDto(StudentsTutors studentsTutors);
  StudentsTutorsTDto toTDto(StudentsTutors studentsTutors);

  Set<StudentsTutorsTDto> toTDtoSet(Set<StudentsTutors> studentsTutorsSet);
  Set<StudentsTutorsSDto> toSDtoSet(Set<StudentsTutors> studentsTutorsSet);

  Set<StudentsTutors> toEntitiesFromTDto(Set<StudentsTutorsTDto> studentsTutorsSet);
  Set<StudentsTutors> toEntitiesFromSDto(Set<StudentsTutorsSDto> studentsTutorsSet);
}
