package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.StudentPersonalInfoDto;
import com.example.easy_learning.model.StudentPersonalInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentPersonalInfoMapper {
  StudentPersonalInfoDto toDto(StudentPersonalInfo studentPersonalInfo);
  StudentPersonalInfo toEntity(StudentPersonalInfoDto studentPersonalInfoDto);
}
