package com.example.easy_learning.model;

import jakarta.persistence.*;

@Entity
@Table(name = "task")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "photo", nullable = false)
    private byte[] photo; // LONGBLOB маппится на byte[]

    @Column(name = "class", nullable = false)
    private String className; // Используем "className" вместо "class"

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "difficulty", nullable = false)
    private Integer difficulty;

    @Column(name = "tutor_id", nullable = false)
    private Integer tutorId;

    @ManyToOne
    @JoinColumn(name = "tutor_id", insertable = false, updatable = false)
    private Tutor tutor;

    // Геттеры и сеттеры
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getTutorId() {
        return tutorId;
    }

    public void setTutorId(Integer tutorId) {
        this.tutorId = tutorId;
    }

    public Tutor getTutor() {
        return tutor;
    }

    public void setTutor(Tutor tutor) {
        this.tutor = tutor;
    }
}