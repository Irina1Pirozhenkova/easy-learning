package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.HomeworkNRDto;
import com.example.easy_learning.dto.HomeworkRDto;
import com.example.easy_learning.dto.StudentNRDto;
import com.example.easy_learning.model.Homework;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface HomeworkMapper {

  Homework toNREntity(HomeworkNRDto homeworkNRDto);

  HomeworkNRDto toNRDto(Homework homework);

  HomeworkRDto toRDto(HomeworkNRDto homeworkNRDto);
  HomeworkNRDto toNRDto(HomeworkRDto homeworkRDto);

  Set<Homework> toNREntities(Set<HomeworkNRDto> homeworkNRDtos);

  Set<HomeworkNRDto> toNRDtos(Set<Homework> homeworks);
  Set<HomeworkRDto> toRDtos(Set<HomeworkNRDto> homeworkNRDtoss);
}
