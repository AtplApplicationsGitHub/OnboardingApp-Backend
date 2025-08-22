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
public class TaskQuestionsDTO {

    private String id;
    private String questionId;
    private String response;
    private String responseType;
    private String status;
    private String complianceDay;
    private Boolean overDueFlag;

}