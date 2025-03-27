package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.StudentsHomeworkHDto;
import com.example.easy_learning.dto.StudentsHomeworkSDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {HomeworkMapper.class, StudentMapper.class})
public interface StudentsHomework {
  StudentsHomeworkHDto toHDto(StudentsHomework studentsHomework);
  StudentsHomeworkSDto toSDto(StudentsHomework studentsHomework);
}
