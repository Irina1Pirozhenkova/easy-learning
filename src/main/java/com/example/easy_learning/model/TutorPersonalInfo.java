package com.example.easy_learning.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class TutorPersonalInfo {
    @Column(name = "firstname")
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(name = "phone", unique = true)
    private String phone;

    @Column(name = "telegram", unique = true)
    private String telegram;
}