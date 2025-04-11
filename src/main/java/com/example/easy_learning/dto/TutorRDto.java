package com.example.easy_learning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Set;

@Data // Оставь как есть
@NoArgsConstructor
@AllArgsConstructor // <-- добавь, если будешь использовать полные конструкторы
public class TutorRDto {
  private Integer id;

  private TutorPersonalInfoDto personalInfo;

  private String password;

  private Set<TaskNRDto> tasks;

  private Set<HomeworkNRDto> homeworks;

  private Set<StudentNRDto> students;
}
