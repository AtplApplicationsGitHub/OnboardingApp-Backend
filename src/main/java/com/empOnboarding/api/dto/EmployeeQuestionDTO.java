package com.empOnboarding.api.dto;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class EmployeeQuestionDTO {

    private String id;
    private String question;
    private String responseType;
    private String response;
    private Boolean completedFlag;

}