package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SubjectClassPair {

    @Enumerated(EnumType.STRING)
    @Column(name = "subject")
    private Subject subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "class_level")
    private ClassLevel classLevel;
}