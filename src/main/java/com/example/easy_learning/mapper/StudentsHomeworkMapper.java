package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.StudentsHomeworkHDto;
import com.example.easy_learning.dto.StudentsHomeworkSDto;
import com.example.easy_learning.model.StudentsHomework;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {HomeworkMapper.class, StudentMapper.class})
public interface StudentsHomeworkMapper {
  StudentsHomeworkHDto toHDto(StudentsHomework studentsHomework);
  StudentsHomeworkSDto toSDto(StudentsHomework studentsHomework);



  Set<StudentsHomeworkHDto> toHDtoSet(Set<StudentsHomework> studentsHomeworkSet);
  Set<StudentsHomeworkSDto> toSDtoSet(Set<StudentsHomework> studentsHomeworkSet);

  Set<StudentsHomework> toEntitiesFromSDto(Set<StudentsHomeworkSDto> studentsHomeworkSet);
  Set<StudentsHomework> toEntitiesFromHDto(Set<StudentsHomeworkHDto> studentsHomeworkSet);
}
