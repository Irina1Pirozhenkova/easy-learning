package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.Set;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class PersonalInfo {

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

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "user_subject_classes",
        joinColumns = @JoinColumn(name = "user_id")
    )
    private Set<SubjectClassPair> subjectClassPairs;
}