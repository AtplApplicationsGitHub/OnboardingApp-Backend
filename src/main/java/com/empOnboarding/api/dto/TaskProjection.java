package com.empOnboarding.api.dto;

public interface TaskProjection {
    Long getEmployeeId();
    String getName();
    String getDepartment();
    String getRole();
    String getLevel();
    String getTaskIds();
    Long getTotalQuestions();
    Long getCompletedQuestions();
    Long getPendingQuestions();
    String getStatus();
}