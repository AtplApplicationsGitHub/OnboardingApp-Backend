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
public class GroupsDTO {
	
	private String id;
	private String name;
	private String pgLead;
	private String egLead;
	private String createdTime;
	private String updatedTime;
	private long quesCount;
	private Boolean deleteFlag;

	
}