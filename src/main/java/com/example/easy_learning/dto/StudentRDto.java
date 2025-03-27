package com.example.easy_learning.dto;

import com.example.easy_learning.model.StudentsHomework;
import lombok.Data;

import java.util.Set;

@Data
public class StudentRDto {

  private Integer id;

  private StudentPersonalInfoDto studentPersonalInfo;

  private Set<StudentsHomeworkHDto> homeworks;

  private Set<TutorNRDto> tutors;
}
