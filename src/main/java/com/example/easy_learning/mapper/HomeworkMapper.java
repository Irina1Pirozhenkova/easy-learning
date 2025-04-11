package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.HomeworkNRDto;
import com.example.easy_learning.dto.HomeworkRDto;
import com.example.easy_learning.dto.StudentNRDto;
import com.example.easy_learning.model.Homework;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface HomeworkMapper {

  Homework toNREntity(HomeworkNRDto homeworkNRDto);

  HomeworkNRDto toNRDto(Homework homework);

  HomeworkRDto toRDto(HomeworkNRDto homeworkNRDto);
  HomeworkNRDto toNRDto(HomeworkRDto homeworkRDto);

  List<Homework> toNREntities(List<HomeworkNRDto> homeworkNRDtos);

  Set<HomeworkNRDto> toNRDtos(Set<Homework> homeworks);
  List<HomeworkRDto> toRDtos(List<HomeworkNRDto> homeworkNRDtoss);
}
