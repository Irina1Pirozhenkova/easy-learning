package com.example.easy_learning.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class HomeworkNRDto {

  private Integer id;

  private String className; // Используем "className" вместо "class"

  private String subject;

  private String topic;

  private Integer difficulty;
}
