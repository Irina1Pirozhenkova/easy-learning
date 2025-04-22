package com.example.easy_learning.mapper;

import com.example.easy_learning.dto.RegisterDto;
import com.example.easy_learning.dto.TutorNRDto;
import com.example.easy_learning.dto.TutorRDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {TutorPersonalInfoMapper.class})
public interface TutorMapper {

//  Tutor toNREntity(TutorNRDto tutorNRDto);
//  TutorRDto toRDto(Tutor tutor); // <-- этот нужен в контроллере
//  Tutor toEntity(TutorRDto tutorRDto); // <-- для обратного преобразования
//
//  @Mapping(target = "personalInfo", source = "personalInfo")
//  TutorNRDto toNRDto(Tutor tutor);
//  TutorNRDto toNRDto(TutorRDto tutor);
//  TutorRDto toRDto(TutorNRDto tutorNRDto);
//
//  Set<Tutor> toNREntities(Set<TutorNRDto> tutorNRDtos);
//
//  Set<TutorNRDto> toNRDtos(Set<Tutor> tutors);
//  Set<TutorRDto> toRDtos(Set<TutorNRDto> tutorNRDtoss);
//
//  Tutor toTutor(RegisterDto registerDto);
}
