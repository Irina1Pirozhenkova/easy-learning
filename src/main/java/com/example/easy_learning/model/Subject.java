package com.example.easy_learning.model;

public enum Subject {
    MATH("Математика"),
    PHYSICS("Физика"),
    CHEMISTRY("Химия"),
    BIOLOGY("Биология"),
    LITERATURE("Литература"),
    HISTORY("История"),
    RUSSIAN("Русский язык"),
    ENGLISH("Английский язык");

    private final String displayName;

    Subject(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
