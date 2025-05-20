package com.example.easy_learning.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "task")
@Getter
@Setter
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "photo_url", nullable = false)
    private String photoUrl;
    @Enumerated(EnumType.STRING)
    @Column(name = "class", nullable = false)
    private ClassLevel className;
    @Enumerated(EnumType.STRING)
    @Column(name = "subject", nullable = false)
    private Subject subject;
    @Column(name = "topic", nullable = false)
    private String topic;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "difficulty", nullable = false)
    private Integer difficulty;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id")
    private User tutor;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentsTasks> studentsTasks = new HashSet<>();
}
