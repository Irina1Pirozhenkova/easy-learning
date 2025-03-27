package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "student")
@Getter
@Setter
@NoArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private StudentPersonalInfo studentPersonalInfo;

    @Column(name = "password", nullable = false)
    private String password;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentsHomework> homeworks = new HashSet<>();

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentsTutors> tutors = new HashSet<>();

    public void setHomeworks(Set<StudentsHomework> homeworks) {
        this.homeworks.clear();
        homeworks.forEach(h -> h.setStudent(this));
    }

    public void setTutors(Set<StudentsTutors> tutors) {
        this.tutors.clear();
        tutors.forEach(t -> t.setStudent(this));
    }
}