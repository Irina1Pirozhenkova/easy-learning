package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.TaskNRDto;
import com.example.easy_learning.dto.TaskRDto;
import com.example.easy_learning.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface TaskMapper {

  @Mapping(target = "tutor.id", source = "tutorId")
  Task toNREntity(TaskNRDto taskNRDto);

  @Mapping(target = "tutorId", source = "tutor.id")
  TaskNRDto toNRDto(Task task);
  TaskRDto toRDto(TaskNRDto taskNRDto);

  Set<Task> toNREntities(Set<TaskNRDto> taskNRDtos);

  List<TaskNRDto> toNRDtos(List<Task> tasks);
  Set<TaskRDto> toRDtos(Set<TaskNRDto> taskNRDtoss);
}
