package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "role")
  private Set<Role> roles = new HashSet<>();

  // Встраиваемая персональная информация
  @Embedded
  private PersonalInfo personalInfo;

  // Задания, созданные репетитором
  @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Task> tasks = new HashSet<>();

  // Связи ученик–домзадание
  @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<StudentsTasks> studentsTasks = new HashSet<>();

  // Связи ученик–тьютор (роль STUDENT)
  @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<StudentsTutors> tutors = new HashSet<>();

  @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<StudentsTutors> students = new HashSet<>();
}
