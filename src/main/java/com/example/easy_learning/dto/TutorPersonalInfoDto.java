package com.example.easy_learning.dto;

import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class TutorPersonalInfoDto {

  private String firstname;

  private String lastname;

  private LocalDate birthdate;

  private String email;

  private String phone;

  private String telegram;
}
