package com.example.easy_learning.model;

public enum ClassLevel {
  CLASS_1("Класс 1"),
  CLASS_2("Класс 2"),
  CLASS_3("Класс 3"),
  CLASS_4("Класс 4"),
  CLASS_5("Класс 5"),
  CLASS_6("Класс 6"),
  CLASS_7("Класс 7"),
  CLASS_8("Класс 8"),
  CLASS_9("Класс 9"),
  CLASS_10("Класс 10"),
  CLASS_11("Класс 11");

  private final String displayName;

  ClassLevel(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}

