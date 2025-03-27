package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.TutorNRDto;
import com.example.easy_learning.dto.TutorRDto;
import com.example.easy_learning.model.Tutor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {TutorPersonalInfoMapper.class})
public interface TutorMapper {

  Tutor toNREntity(TutorNRDto tutorNRDto);

  @Mapping(target = "personalInfo", source = "personalInfo")
  TutorNRDto toNRDto(Tutor tutor);
  TutorRDto toRDto(TutorNRDto tutorNRDto);

  Set<Tutor> toNREntities(Set<TutorNRDto> tutorNRDtos);

  Set<TutorNRDto> toNRDtos(Set<Tutor> tutors);
  Set<TutorRDto> toRDtos(Set<TutorNRDto> tutorNRDtoss);
}
