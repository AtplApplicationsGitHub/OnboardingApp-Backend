package com.empOnboarding.api.dto;

import com.empOnboarding.api.entity.QuestionLevel;
import com.empOnboarding.api.entity.TaskQuestions;
import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class TaskViewDTO {

    private String empName;
    private List<String> taskId;
    private String department;
    private String role;
    private String completedCount;
    private String totalCount;

}