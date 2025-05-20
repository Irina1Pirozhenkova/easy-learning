package com.example.easy_learning.dto;

import java.time.LocalDate;
import lombok.*;
import lombok.Data;

@Getter
@Setter
@NoArgsConstructor
@Data
public class TutorPersonalInfoDto {
  private String firstname;
  private String lastname;
  private LocalDate birthdate;
  private String phone;
  private String telegram;
}

