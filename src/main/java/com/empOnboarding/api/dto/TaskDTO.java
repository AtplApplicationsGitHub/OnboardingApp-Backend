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
public class TaskDTO {

	private String id;
	private String employeeName;
	private String groupName;
	private String level;
	private String department;
	private String role;
	private String lab;
	private String pastExperience;
	private String prevCompany;
	private String complianceDay;
	private String assignedTo;
	private int totalQuestions;
	private long completedQuestions;
	private String status;
	private String doj;
	private List<TaskQuestionsDTO> questionList;
	private String createdTime;
	private String updatedTime;
	private List<String> taskId;



}
