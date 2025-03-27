package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.Getter;

@Entity
@Table(name = "students_tutors")
@Getter
@Setter
@NoArgsConstructor
public class StudentsTutors {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "student_id")
  private Student student;

  @ManyToOne
  @JoinColumn(name = "tutor_id")
  private Tutor tutor;

  public void setStudent(Student student) {
    this.student = student;
    if (student != null && student.getTutors() != null) student.getTutors().add(this);
  }

  public void setTutor(Tutor tutor) {
    this.tutor = tutor;
    if (tutor != null && tutor.getStudents() != null) tutor.getStudents().add(this);
  }
}