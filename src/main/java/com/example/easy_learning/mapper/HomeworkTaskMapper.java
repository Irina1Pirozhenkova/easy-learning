package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.HomeworkTaskHDto;
import com.example.easy_learning.dto.HomeworkTaskTDto;
import com.example.easy_learning.model.Homework;
import com.example.easy_learning.model.HomeworkTask;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {HomeworkMapper.class, TaskMapper.class})
public interface HomeworkTaskMapper {
  HomeworkTaskHDto toHDto(HomeworkTask homeworkTask);
  HomeworkTaskTDto toTDto(HomeworkTask homeworkTask);

  Set<HomeworkTaskHDto> toHDtos(Set<HomeworkTask> homeworkTasks);
  Set<HomeworkTaskTDto> toTDtos(Set<HomeworkTask> homeworkTasks);

  Set<HomeworkTask> toEntitiesFromHDtos(Set<HomeworkTaskHDto> homeworkTaskHDtos);
  Set<HomeworkTask> toEntitiesFromTDtos(Set<HomeworkTaskTDto> homeworkTaskTDtos);
}
