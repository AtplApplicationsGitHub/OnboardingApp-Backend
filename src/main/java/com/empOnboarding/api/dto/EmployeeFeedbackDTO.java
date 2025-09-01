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
public class EmployeeFeedbackDTO {

	private String id;
	private String star;
	private String feedback;
	private String taskId;
	private Boolean completed;

}
