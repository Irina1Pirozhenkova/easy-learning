package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.TutorPersonalInfoDto;
import com.example.easy_learning.model.Tutor;
import com.example.easy_learning.model.TutorPersonalInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TutorPersonalInfoMapper {

  TutorPersonalInfoDto toTutorPersonalInfoDto(TutorPersonalInfo tutorPersonalInfo);
  TutorPersonalInfo toTutorPersonalInfo(TutorPersonalInfoDto tutorPersonalInfoDto);
}
