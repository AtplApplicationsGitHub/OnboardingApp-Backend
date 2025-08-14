package com.empOnboarding.api.dto;

import java.util.List;

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
public class QuestionsDTO extends CommonDTO{
	
	private String id;
	private String text;
	private String response;
	private String complainceDay;
	private List<String> questionLevel;
	private String groupId;
	private String createdTime;
	private String updatedTime;
	
}